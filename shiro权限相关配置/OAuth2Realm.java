package cn.proflu.profluweb.common.oauth2;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import cn.proflu.profluweb.common.exception.ThisExcepton;
import cn.proflu.profluweb.pojo.back.SysUser;
import cn.proflu.profluweb.pojo.back.SysUserToken;
import cn.proflu.profluweb.service.back.ShiroService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 认证身份认证realm; (这个需要自己写，账号密码校验；权限等)
 *
 * @date 2017-05-20 14:00
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {
    @Resource
    private ShiroService shiroService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /**
     * 授权(验证权限时调用)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SysUser user = (SysUser)principals.getPrimaryPrincipal();
        Long userId = user.getUserId();
        //用户权限列表
        Set<String> permsSet = shiroService.getUserPermissions(userId);

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        //用户接口权限、、这里要根据用户查询可以访问的接口有哪些。
        permsSet.add("sys:menu:lists");//模拟查询出来的接口权限
        info.setStringPermissions(permsSet);
        return info;
    }

    /**
     * 认证(登录时调用) 认证信息.(身份验证) : Authentication 是用来验证用户身份
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        
    	String accessToken = (String) token.getPrincipal();

        //根据accessToken，查询用户信息
		SysUserToken tokenEntity = shiroService.queryByToken(accessToken);
        //token失效
		
        if(tokenEntity == null || DateTime.parse(tokenEntity.getExpireTime().toString()).isBefore(DateTime.now())){
            throw new IncorrectCredentialsException("token is invalid");
        }
        //查询用户信息
        SysUser user = shiroService.queryUser(tokenEntity.getUserId());
        //账号锁定
        if(user.getStatus() == 0){
            throw new ThisExcepton("账号已被锁定,请联系管理员");
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());
        return info;
    }
}
