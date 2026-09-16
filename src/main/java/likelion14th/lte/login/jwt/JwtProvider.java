package likelion14th.lte.login.jwt;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component

public class JwtProvider {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long accessExpMs;
    private final long refreshExpMs;

    public JwtProvider(
            JwtEncoder jwtEncoder,
            @Qualifier("refreshTokenDecoder") JwtDecoder jwtDecoder,
            @Value("${jwt.access-exp-ms:3600000}") long accessExpMs,
            @Value("${jwt.refresh-exp-ms:1209600000}") long refreshExpMs
    ){
        this.jwtEncoder= jwtEncoder;
        this.jwtDecoder=jwtDecoder;
        this.accessExpMs=accessExpMs;
        this.refreshExpMs=refreshExpMs;
    }

    public String createAccessToken(Long userId){
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plusMillis(accessExpMs))
                .claim("type", "ACCESS")
                .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    }
    public String createRefreshToken(Long userId){
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plusMillis(refreshExpMs))
                .claim("type", "REFRESH")
                .build();
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    }

    public Long getRefreshTokenExpiration(){
        return System.currentTimeMillis() + refreshExpMs;
    }
    public Long validateRefreshToken(String refreshToken){
        Jwt jwt = jwtDecoder.decode(refreshToken);
        return Long.parseLong(jwt.getSubject());
    }
}
