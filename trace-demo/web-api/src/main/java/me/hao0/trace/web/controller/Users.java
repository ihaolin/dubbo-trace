package me.hao0.trace.web.controller;

import me.hao0.trace.user.model.User;
import me.hao0.trace.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
@Controller
@RequestMapping("/api/users")
public class Users {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public User query(@PathVariable(value = "id") Long id){
        return userService.findById(id);
    }
}
