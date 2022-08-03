package com.codename1.app.backend.webservices;

import com.codename1.app.backend.data.CategoryEntity;
import com.codename1.app.backend.data.DishEntity;
import com.codename1.app.backend.data.DishRepository;
import com.codename1.app.backend.data.RestaurantEntity;
import com.codename1.app.backend.data.RestaurantRepository;
import com.codename1.app.backend.service.dao.AppVersion;
import com.codename1.app.backend.service.BuildAppAsync;
import com.codename1.app.backend.service.dao.Dish;
import com.codename1.app.backend.service.dao.DishContainer;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
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
@RequestMapping("/doBuildApp")
public class BuildAppService {
    @Autowired
    private BuildAppAsync buildAsync;
    
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody String build(@RequestParam(name = "secret", required = true) String secret, 
            @RequestParam(name = "pushKey", required = false) String pushKey,
            @RequestParam(name = "targetType", required = true) String targetType) {
        buildAsync.buildApp(secret, pushKey, targetType);
        return "OK";
    }
}
