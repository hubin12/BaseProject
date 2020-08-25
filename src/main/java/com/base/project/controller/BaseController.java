package com.base.project.controller;

import com.base.project.common.Result;
import com.base.project.common.ResultCodeEnum;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器
 *
 * @author mrbeard
 * @date 2020/08/25
 */
@RestController
public class BaseController {

    @PostMapping("/hello")
    public Result hello(){
        return new Result(ResultCodeEnum.SUCCESS);
    }
}
