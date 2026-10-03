package com.example.ai.mapper;

import com.example.ai.entity.AiToolCallLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * AI 工具调用审计日志数据接口
 */
public interface AiToolCallLogMapper {

    @Insert("insert into ai_tool_call_log(chat_log_id, tool_name, arguments, result, success, error_msg, duration_ms, create_time) " +
            "values(#{chatLogId}, #{toolName}, #{arguments}, #{result}, #{success}, #{errorMsg}, #{durationMs}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiToolCallLog toolCallLog);

    @Select("select * from ai_tool_call_log where chat_log_id = #{chatLogId} order by id")
    List<AiToolCallLog> selectByChatLogId(Integer chatLogId);

    /** 各工具被调用次数 / 失败次数，放在管理端看"模型到底爱用哪个工具" */
    @Select("select tool_name as toolName, count(*) as total, sum(case when success = 0 then 1 else 0 end) as failed " +
            "from ai_tool_call_log group by tool_name order by total desc")
    List<Map<String, Object>> selectToolUsageStats();
}
