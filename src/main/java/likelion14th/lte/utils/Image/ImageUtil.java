package likelion14th.lte.utils.Image;

import likelion14th.lte.utils.exception.UtilException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;

import static likelion14th.lte.utils.exception.UtilException.Reason.*;

@Slf4j
@Component
public class ImageUtil {

    private static final Tika tika = new Tika();

    public record ResizedImage(byte[] bytes, String contentType, String extension) {}

    private static final Set<String> ALLOWED = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    private static final long MAX_SIZE = 50L * 1024 * 1024; // 50MB

    public void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw new UtilException(FILE_EMPTY);
        }

        if (file.getSize() > MAX_SIZE) {
            throw new UtilException(FILE_TOO_LARGE);
        }

        try (InputStream is = file.getInputStream()) {
            String detectedType = tika.detect(is);
            if (!ALLOWED.contains(detectedType)) {
                throw new UtilException(TYPE_NOT_ALLOWED);
            }
        } catch (UtilException e) {
            throw e; // 그대로 전파
        } catch (Exception e) {
            log.warn("Image validation failed", e);
            throw new UtilException(IMAGE_PROCESS_FAILED, e);
        }
    }

    public ResizedImage resizeProfileToPngBytes(MultipartFile file, int size) {

        try {
            BufferedImage output;
            try (InputStream is = file.getInputStream()) {
                output = Thumbnails.of(is)
                        .size(size, size)
                        .crop(Positions.CENTER)
                        .asBufferedImage();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean ok = ImageIO.write(output, "png", baos);

            if (!ok) {
                throw new UtilException(IMAGE_PROCESS_FAILED);
            }

            return new ResizedImage(
                    baos.toByteArray(),
                    "image/png",
                    "png"
            );

        } catch (UtilException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Image resize failed", e);
            throw new UtilException(IMAGE_PROCESS_FAILED, e);
        }
    }
}
