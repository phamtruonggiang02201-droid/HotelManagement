package com.example.HM.service;

import com.example.HM.dto.RoomDTO;
import com.example.HM.dto.RoomRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface RoomService {
    RoomDTO getRoomById(String id);

    // Excel Operations
    ByteArrayInputStream exportRoomsToExcel();
    void importRoomsFromExcel(MultipartFile file);
}
