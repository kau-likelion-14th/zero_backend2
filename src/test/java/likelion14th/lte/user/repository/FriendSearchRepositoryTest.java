package likelion14th.lte.user.repository;

import jakarta.persistence.EntityManagerFactory;
import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.follow.repository.FollowRepository;
import likelion14th.lte.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

// 애플리케이션의 .env나 원격 DB를 읽지 않고 메모리 DB에서 실제 JPQL을 실행합니다.
@SpringJUnitConfig(FriendSearchRepositoryTest.DatabaseConfig.class)
@Transactional
class FriendSearchRepositoryTest {
    @Autowired UserRepository users;
    @Autowired FollowRepository follows;

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {UserRepository.class, FollowRepository.class})
    static class DatabaseConfig {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:friend-search;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("likelion14th.lte");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }

        @Bean
        JpaTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
    }

    @Test
    void searchFiltersByNicknameAndExcludesSelfAndAlreadyFollowedUsers() {
        User me = save("전성 본인", "ME");
        User followed = save("전성 팔로잉", "FOLLOWED");
        User candidate = save("전성환", "CANDIDATE");
        save("다른 이름", "OTHER");
        follows.saveAndFlush(Follow.builder().fromUser(me).toUser(followed).build());

        var result = users.searchCanFollowUsers(me.getId(), "전성", PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(User::getId).containsExactly(candidate.getId());
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void searchIsCaseInsensitiveAndPaginatesWithStableOrder() {
        User me = save("me", "ME");
        User first = save("ALICE one", "ONE");
        User second = save("alice two", "TWO");
        var pageZero = users.searchCanFollowUsers(me.getId(), "aLiCe", PageRequest.of(0, 1));
        var pageOne = users.searchCanFollowUsers(me.getId(), "aLiCe", PageRequest.of(1, 1));
        assertThat(pageZero.getContent()).extracting(User::getId).containsExactly(first.getId());
        assertThat(pageOne.getContent()).extracting(User::getId).containsExactly(second.getId());
        assertThat(pageZero.getTotalPages()).isEqualTo(2);
    }

    @Test
    void escapedWildcardMatchesLiteralNicknameAndNotEveryUser() {
        User me = save("me", "ME");
        User literal = save("100%_친구!", "LITERAL");
        save("100xx친구", "OTHER");
        var result = users.searchCanFollowUsers(me.getId(), "100!%!_친구!!", PageRequest.of(0, 10));
        assertThat(result.getContent()).extracting(User::getId).containsExactly(literal.getId());
    }

    @Test
    void noMatchReturnsEmptyPage() {
        User me = save("me", "ME");
        assertThat(users.searchCanFollowUsers(me.getId(), "없는닉네임", PageRequest.of(0, 10)).getContent()).isEmpty();
    }

    private User save(String nickname, String tag) {
        return users.saveAndFlush(User.builder().username(nickname).userTag(tag).build());
    }
}
