package com.kyawhsan.notiva.common.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.kyawhsan.notiva.common.response.ImageUploadResponse;
import com.kyawhsan.notiva.common.exception.CloudinaryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public ImageUploadResponse uploadImage(
            MultipartFile file,
            String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", folder, "resource_type", "image"));

            return new ImageUploadResponse(result.get("secure_url").toString(),
                    result.get("public_id").toString());
        } catch (IOException exception) {
            throw new CloudinaryException("Unable to upload image", exception);
        }
    }

    public void deleteImage(
            String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", "image", "invalidate", true));
        } catch (IOException exception) {
            throw new CloudinaryException("Unable to delete image", exception);
        }
    }
}