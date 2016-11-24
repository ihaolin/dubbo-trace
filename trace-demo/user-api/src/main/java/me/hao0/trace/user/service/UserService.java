package me.hao0.trace.user.service;

import me.hao0.trace.user.model.Addr;
import me.hao0.trace.user.model.User;
import java.util.List;

/**
 * Author: haolin
 * Email:  haolin.h0@gmail.com
 */
public interface UserService {

   User findById(Long id);

   List<Addr> myAddrs(Long userId);
}
