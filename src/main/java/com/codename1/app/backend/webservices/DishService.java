package com.codename1.app.backend.webservices;

import com.codename1.app.backend.data.CategoryEntity;
import com.codename1.app.backend.data.DishEntity;
import com.codename1.app.backend.data.DishRepository;
import com.codename1.app.backend.data.RestaurantEntity;
import com.codename1.app.backend.data.RestaurantRepository;
import com.codename1.app.backend.service.dao.AppVersion;
import com.codename1.app.backend.service.dao.Dish;
import com.codename1.app.backend.service.dao.DishContainer;
import com.codename1.app.backend.service.dao.RestaurantRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/dish")
public class DishService {
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private DishRepository dishRepository;

    @RequestMapping(method=RequestMethod.GET)
    public @ResponseBody DishContainer fetchDishes(@RequestParam(value="stamp", required=true) long stamp,
            @RequestParam(value="appId", required=true) String appId) {
        RestaurantEntity r = restaurantRepository.findOne(appId);
        if(r.getDishListUpdateTimestamp() != stamp) {
            Set<DishEntity> dd = r.getDishes();
            Dish[] a = new Dish[dd.size()];
            int pos = 0;
            for(DishEntity e : dd) {
                a[pos] = e.toDish();
                pos++;
            }
            Set<CategoryEntity> cats = r.getCategories();
            Iterator<CategoryEntity> catIterator = cats.iterator();
            String[] categories = new String[cats.size()];
            for(int iter = 0 ; iter < categories.length ; iter++) {
                categories[iter] = catIterator.next().getName();
            }
            return new DishContainer("" + r.getDishListUpdateTimestamp(), a, categories);
        }
        return new DishContainer(null, new Dish[0], new String[0]);
    }
    
    @RequestMapping(method = RequestMethod.PUT)
    public @ResponseBody String update(@RequestParam(name="secret", required = true) String secret,
            @RequestParam(name="d", required = true) Dish d) throws IOException {
        RestaurantEntity e = restaurantRepository.findBySecret(secret);
        DishEntity de = null;
        if(d.getId() == null) {
            de = new DishEntity();
            Set<DishEntity> dishes = e.getDishes();
            dishes.add(de);
        } else {
            for(DishEntity cde : e.getDishes()) {
                if(cde.getId().toString().equals(d.getId())) {
                    de = cde;
                    break;
                }
            }
        }
        CategoryEntity ce = null;
        for(CategoryEntity current : e.getCategories()) {
            if(current.getName().equals(d.getCategory())) {
                ce = current;
                break;
            }
        }
        
        // de can be null in case of a security exploit in which case a null pointer exception will
        // block the attack
        de.setCategory(ce);
        
        de.setDescription(d.getDescription());
        de.setName(d.getName());
        de.setPrice(d.getPrice());

        dishRepository.save(de);
        if(d.getId() == null) {
            restaurantRepository.save(e);
        }
        
        return de.getId().toString();
    }
    
    
    @RequestMapping(method = RequestMethod.DELETE)
    public void delete(@RequestParam(name="secret", required = true) String secret,
            @RequestParam(name="dId", required = true) Long dishId) throws IOException {
        RestaurantEntity e = restaurantRepository.findBySecret(secret);
        for(DishEntity d : e.getDishes()) {
            if(d.getId().equals(dishId)) {
                HashSet<DishEntity> de = new HashSet<>();
                de.addAll(e.getDishes());
                de.remove(d);
                e.setDishes(de);
                long time = System.currentTimeMillis();
                e.setDishListUpdateTimestamp(time);
                restaurantRepository.save(e);
                
                d.setDeleteTime(time);
                dishRepository.save(d);
                return;
            }
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody String updateImage(@RequestParam(name="secret", required = true) String secret,
            @RequestParam(name="img", required = true) MultipartFile img,
            @RequestParam(name="d", required = true) String d) throws IOException {
        RestaurantEntity e = restaurantRepository.findBySecret(secret);
        DishEntity de = null;
        for(DishEntity cde : e.getDishes()) {
            if(cde.getId().toString().equals(d)) {
                de = cde;
                break;
            }
        }
        
        de.setImageData(img.getBytes());
        
        dishRepository.save(de);
        
        return de.getId().toString();
    }
    
}