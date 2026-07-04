-- 指定日の日報ログ件数取得
-- プレースホルダ: {dataset}, {table}
-- パラメータ: @logDate

SELECT COUNT(*) as cnt
FROM `{dataset}.{table}`
WHERE log_date = DATE(@logDate)
