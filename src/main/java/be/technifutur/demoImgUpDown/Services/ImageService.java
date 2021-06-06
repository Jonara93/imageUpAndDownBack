package be.technifutur.demoImgUpDown.Services;

import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ImageService {
    public final String storageDirectoryPath = "src\\main\\resources\\static\\image";

    public ResponseEntity uploadToLocalFileSystem(MultipartFile file) {
        /* we will extract the file name (with extension) from the given file to store it in our local machine for now
        and later in virtual machine when we'll deploy the project */
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        /* The Path in which we will store our image . we could change it later based on the OS of the virtual machine in which we will deploy the project.
        In my case i'm using windows 10.*/
        Path storageDirectory = Paths.get(storageDirectoryPath);

        /* we'll do just a simple verification to check if the folder in which we will store our images exists or not */
        if (!Files.exists(storageDirectory)) {
            try {
                Files.createDirectories(storageDirectory); // create the directory if not exist
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //path destination for the images
        Path destinationPath = Paths.get(storageDirectory.toString() + "\\" + fileName);

        try {
            //trying to reduce img
            //Convert multipartfile to bufferedImage
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            //Reduce image
//            bufferedImage = simpleResizeImage(bufferedImage, (int) (bufferedImage.getWidth() * 0.5), (int) (bufferedImage.getHeight() * 0.5));
            bufferedImage = simpleResizeImage(bufferedImage, 600, (int) (bufferedImage.getHeight() * 0.5));
            //Convert bufferedImage to a InputStream
//            String formatName = file.getContentType();
//            formatName = formatName.replace("image/","");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, file.getContentType().replace("image/",""), byteArrayOutputStream);
            InputStream imageResize = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            // we are Copying all bytes from an input stream to a file
            //With the normal img
//            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            //With the reduce img
            Files.copy(imageResize, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // the response will be the download URL of the image
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/images/getImage/")
                .path(fileName)
                .toUriString();
        System.out.println(fileDownloadUri);
        return ResponseEntity.ok(fileDownloadUri);
    }

    public byte[] getImageWithMediaType(String imageName) throws IOException {
        //retrieve the image by its name
        Path destinationPath = Paths.get(storageDirectoryPath + "\\" + imageName);

        return IOUtils.toByteArray(destinationPath.toUri());
    }

    BufferedImage simpleResizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws Exception {
        return Scalr.resize(originalImage, Scalr.Method.BALANCED, Scalr.Mode.AUTOMATIC, targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
    }
}
