SELECT SUBSTRING( SUBSTRING( object, POSITION('//' IN object)+2 ), POSITION('/' IN SUBSTRING(object,POSITION('//' IN object)+2)) ) \"events__page\", \"actor\".school_id \"sessions__school_id\", date_trunc('year', (\"events\".time::timestamptz AT TIME ZONE 'UTC')) \"events__time_stamp_year\", count(\"events\".actor) \"events__count\" 

FROM ( SELECT *, CAST(context ->> 'first-paint' AS INT) AS firstPaint, CAST(context ->> 'time-to-interactive' AS INT) AS timeToInteractive, CAST(context ->> 'page-loaded' AS INT) AS pageLoaded, CAST(context ->> 'downlink' AS FLOAT) AS downlink, CAST(context ->> 'response-start' AS INT) AS responseStart, CAST(context ->> 'response-end' AS INT) AS responseEnd, CAST(context ->> 'dom-interactive-time' AS INT) AS domInteractiveTime, CAST(context ->> 'dom-content-loaded' AS INT) AS domContentLoaded FROM activity ) AS \"events\" 

LEFT JOIN ( SELECT row_number() over(partition by event.actor order by event.time) || ' - '|| event.actor as session_id , event.actor , event.time as session_start_at , row_number() over(partition by event.actor order by event.time) as session_sequence , lead(time) over(partition by event.actor order by event.time) as next_session_start_at FROM (SELECT e.actor , e.time , EXTRACT(EPOCH FROM e.time - LAG(e.time) OVER(PARTITION BY e.actor ORDER BY e.time)) AS inactivity_time FROM ( SELECT *, CAST(context ->> 'first-paint' AS INT) AS firstPaint, CAST(context ->> 'time-to-interactive' AS INT) AS timeToInteractive, CAST(context ->> 'page-loaded' AS INT) AS pageLoaded, CAST(context ->> 'downlink' AS FLOAT) AS downlink, CAST(context ->> 'response-start' AS INT) AS responseStart, CAST(context ->> 'response-end' AS INT) AS responseEnd, CAST(context ->> 'dom-interactive-time' AS INT) AS domInteractiveTime, CAST(context ->> 'dom-content-loaded' AS INT) AS domContentLoaded FROM activity ) AS e ) as event WHERE (event.inactivity_time > 1800 OR event.inactivity_time is null) ) AS \"sessions\" 

ON \"events\".actor = \"sessions\".actor AND \"events\".time >= \"sessions\".session_start_at AND (\"events\".time < \"sessions\".next_session_start_at or \"sessions\".next_session_start_at is null) 

LEFT JOIN actor AS \"actor\" ON \"events\".actor = \"actor\".insights_id 

WHERE (\"events\".time >= $1::timestamptz AND \"events\".time <= $2::timestamptz) 

GROUP BY 1, 2, 3 ORDER BY 3 ASC LIMIT 50000","params":["2018-02-03T00:00:00Z","2020-10-30T23:59:59Z"]}





