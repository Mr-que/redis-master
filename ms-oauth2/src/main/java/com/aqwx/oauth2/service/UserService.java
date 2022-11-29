package com.aqwx.oauth2.service;

import com.aqwx.common.model.domain.SignInidentity;
import com.aqwx.common.model.entity.Diners;
import com.aqwx.common.utils.AssertUtil;
import com.aqwx.oauth2.dao.DinersDao;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/*SpringSecurity的用户登陆密码验证*/
@Service
public class UserService implements UserDetailsService {

    @Resource
    private DinersDao dinersDao;

    //username->username or phone or email
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AssertUtil.isNotEmpty(username,"请输入用户名");
        Diners diners = dinersDao.selectByAccountInfo(username);
        if (diners == null) {
            throw new UsernameNotFoundException("用户名或密码错误!");
        }

        //return new User(username,diners.getPassword(),
        //        AuthorityUtils.commaSeparatedStringToAuthorityList(diners.getRoles()));//指定输入用户名和查询到的数据库密码，Security自动做校验

        /*初始化登录认证对象  补充应该是security自动帮我们做密码校验 */
        SignInidentity signInidentity = new SignInidentity();
        BeanUtils.copyProperties(diners,signInidentity);
        return signInidentity;
    }


}
