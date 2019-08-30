package com.armedia.acm.configserver.api;

import com.armedia.acm.configserver.service.FileConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/file")
public class FileConfigurationApiController {

    @Autowired
    FileConfigurationService fileConfigurationService;

    @PostMapping()
    public ResponseEntity moveFileToConfiguration(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("fileName") String fileName) throws Exception {


        fileConfigurationService.moveFileToConfiguration(file, fileName);

        return ResponseEntity.ok().build();
    }

}
