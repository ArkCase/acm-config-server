package com.armedia.acm.configserver.api;

import com.armedia.acm.configserver.service.FileConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(FileConfigurationApiController.class);

    @PostMapping()
    public ResponseEntity moveFileToConfiguration(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("fileName") String fileName) throws Exception {

        logger.info("branding file is received on config server! [{}]", fileName);

        fileConfigurationService.moveFileToConfiguration(file, fileName);

        logger.info("file is moved to config server!");

        return ResponseEntity.ok().build();
    }

}
