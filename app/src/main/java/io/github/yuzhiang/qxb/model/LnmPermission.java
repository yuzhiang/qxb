package io.github.yuzhiang.qxb.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class LnmPermission {
    private int id;
    private String pmName;
    private String pmSummary;
    private int pmIcon;
    private int pmOk;
    private String pmOkSum;

}
