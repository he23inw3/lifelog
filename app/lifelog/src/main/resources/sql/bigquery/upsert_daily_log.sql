-- BigQuery daily_logs テーブルへの冪等 upsert（DELETE + INSERT）
-- プレースホルダ: {dataset}, {table}
-- パラメータ: @slackUserId, @logDate, @rawText, @isHoliday, @tasks,
--             @workHours, @overtimeHours, @diary, @sentiment, @updatedAt, @traceId, @createdAt

DELETE FROM `{dataset}.{table}`
WHERE slack_user_id = CAST(@slackUserId AS STRING) AND log_date = DATE(@logDate);

INSERT INTO `{dataset}.{table}` (
    slack_user_id,
    log_date,
    raw_text,
    is_holiday,
    tasks,
    work_hours,
    overtime_hours,
    diary,
    sentiment,
    trace_id,
    created_at,
    updated_at
) VALUES (
    CAST(@slackUserId AS STRING),
    DATE(@logDate),
    CAST(@rawText AS STRING),
    CAST(@isHoliday AS BOOL),
    CAST(@tasks AS STRING),
    CAST(@workHours AS FLOAT),
    CAST(@overtimeHours AS FLOAT),
    CAST(@diary AS STRING),
    CAST(@sentiment AS STRING),
    CAST(@traceId AS STRING),
    TIMESTAMP(@createdAt),
    TIMESTAMP(@updatedAt)
);
