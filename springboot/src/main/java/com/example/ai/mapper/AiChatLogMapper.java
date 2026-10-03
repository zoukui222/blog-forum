package com.example.ai.mapper;

import com.example.ai.entity.AiChatLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * AI 会话记录数据接口
 */
public interface AiChatLogMapper {

    @Insert("insert into ai_chat_log(user_id, username, role, question, answer, iterations, model, " +
            "prompt_tokens, completion_tokens, duration_ms, status, error_msg, create_time) " +
            "values(#{userId}, #{username}, #{role}, #{question}, #{answer}, #{iterations}, #{model}, " +
            "#{promptTokens}, #{completionTokens}, #{durationMs}, #{status}, #{errorMsg}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiChatLog chatLog);

    @Select("select * from ai_chat_log order by id desc limit #{limit}")
    List<AiChatLog> selectRecent(Integer limit);

    @Select("select ifnull(sum(prompt_tokens), 0) from ai_chat_log")
    Long sumPromptTokens();

    @Select("select ifnull(sum(completion_tokens), 0) from ai_chat_log")
    Long sumCompletionTokens();

    @Select("select count(*) from ai_chat_log")
    Long countAll();

    /** 会话结束时回填结果 */
    @Update("update ai_chat_log set answer = #{answer}, iterations = #{iterations}, " +
            "prompt_tokens = #{promptTokens}, completion_tokens = #{completionTokens}, " +
            "duration_ms = #{durationMs}, status = #{status}, error_msg = #{errorMsg} where id = #{id}")
    int updateResult(AiChatLog chatLog);
}
