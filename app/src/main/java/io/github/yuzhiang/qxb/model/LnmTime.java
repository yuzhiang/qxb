package io.github.yuzhiang.qxb.model;

import lombok.Data;

@Data
public class LnmTime {

    public Long id;
    public String nickName;
    public String avatar;
    public String signature;
    public Long xh = 0L;
    public Integer sum;
    public Integer like = 0;
    public boolean upShow = true;

}
