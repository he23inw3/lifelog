-- 月単位（firstDay〜lastDay）の日報ログ一覧取得
-- プレースホルダ: {dataset}, {table}
-- パラメータ: @slackUserId, @firstDay, @lastDay

SELECT *
FROM `{dataset}.{table}`
WHERE slack_user_id = @slackUserId
  AND log_date BETWEEN DATE(@firstDay) AND DATE(@lastDay)
ORDER BY log_date ASC
