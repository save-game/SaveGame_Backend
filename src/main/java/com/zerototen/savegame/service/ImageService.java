package com.zerototen.savegame.service;

import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Image;
import com.zerototen.savegame.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    public ResponseDto<?> save(Image image){
        return ResponseDto.success(imageRepository.save(image));
    }

}