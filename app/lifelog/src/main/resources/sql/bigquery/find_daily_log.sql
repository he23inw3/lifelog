-- slack_user_id + log_date で1件取得
-- プレースホルダ: {dataset}, {table}
-- パラメータ: @slackUserId, @logDate

SELECT *
FROM `{dataset}.{table}`
WHERE slack_user_id = @slackUserId
  AND log_date      = DATE(@logDate)
LIMIT 1
