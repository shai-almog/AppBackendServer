package com.codename1.app.backend.webservices;

import com.codename1.app.backend.data.CategoryEntity;
import com.codename1.app.backend.data.DishEntity;
import com.codename1.app.backend.data.DishRepository;
import com.codename1.app.backend.data.RestaurantEntity;
import com.codename1.app.backend.data.RestaurantRepository;
import com.codename1.app.backend.service.dao.AppVersion;
import com.codename1.app.backend.service.dao.Dish;
import com.codename1.app.backend.service.dao.DishContainer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/files")
public class FileResponseService {
    public static final File FILES_DIRECTORY = new File(System.getProperty("user.home") + File.separator + "files");
    
    @RequestMapping(value = "/{file_name}", method = RequestMethod.GET)
    public void getFile(
            @PathVariable("file_name") String fileName,
            HttpServletResponse response) throws IOException {
        for(int iter = 0 ; iter < fileName.length() ; iter++) {
            char c = fileName.charAt(iter);
            if(c >= 'a' && c <= 'z' || 
                    c >= '0' && c <= '9' ||
                    c == '.' || c == '-' || c == '_') {
                continue;
            }
            // illegal character that might compromize security
            response.setStatus(404);
            return;
        }
        File f = new File(FILES_DIRECTORY, fileName +".zip");
        
        if(f.exists()) {
            response.setStatus(200);
            try(OutputStream os = response.getOutputStream()) {
                Files.copy(f.toPath(), os);
            }
        } else {
            response.setStatus(404);
        }
            
    }
}
