package com.epr.service;

import com.epr.entity.Image;
import com.epr.repository.ImageRepository;

public class ImageService {
    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Image saveImage(String fileName, String url) {
        Image image = new Image();
        image.setFileName(fileName);
        image.setUrl(url);
        return imageRepository.save(image);
    }
}
