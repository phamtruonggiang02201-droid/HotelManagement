package com.example.HM.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;

public interface RoomTypeService {
    ByteArrayInputStream exportRoomTypesToExcel();
    void importRoomTypesFromExcel(MultipartFile file);
}
