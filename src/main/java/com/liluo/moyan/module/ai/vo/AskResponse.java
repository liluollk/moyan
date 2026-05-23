package com.liluo.moyan.module.ai.vo;

import com.liluo.moyan.module.work.vo.WorkVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AskResponse {

    private String answer;

    private List<WorkVO> references;
}
