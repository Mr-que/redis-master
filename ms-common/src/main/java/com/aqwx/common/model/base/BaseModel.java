package com.aqwx.common.model.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/*实体对象公共属性*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BaseModel implements Serializable {

    private Integer id;
    private Date createTime ;
    private Date updateTime;
    private int isValid;

}