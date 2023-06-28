package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Image;
import com.zerototen.savegame.domain.entity.Post;
import com.zerototen.savegame.repository.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    @Test
    @DisplayName("이미지 저장")
    void save() {
        //given
        Image image = getImage();
        ArgumentCaptor<Image> argumentCaptor = ArgumentCaptor.forClass(Image.class);

        //when
        ResponseDto<?> responseDto = imageService.save(image);

        //then
        assertTrue(responseDto.isSuccess());
        verify(imageRepository).save(argumentCaptor.capture());
        Image saved = argumentCaptor.getValue();
        assertEquals(1L, saved.getId());
        assertEquals("1.jpg", saved.getPostImage());
        assertEquals(getPost().getId(), saved.getPost().getId());
    }

    private Image getImage(){
        return Image.builder()
            .id(1L)
            .postImage("1.jpg")
            .post(getPost())
            .build();
    }

    private Post getPost() {
        return Post.builder()
            .id(1L)
            .build();
    }
}