import React, { useState, useEffect, useRef } from 'react';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import { Box, Typography } from '@mui/material';
import { Calendar as CalendarIcon } from 'lucide-react';
import { logApi } from '../api/logApi';
import { demoApi } from '../api/demoApi';
import type { LogDetailResponse, DemoCalendarListResponse } from '../types';
import dayjs from 'dayjs';
import { LogDetailDialog } from '../components/LogDetailDialog';

/**
 * デモ用のカレンダー表示ページコンポーネント。
 * FullCalendar を用い、Googleカレンダーのデザインに寄せて稼働日・休日を月次カレンダー形式で可視化します。
 * 日付クリックにより該当日報の詳細ダイアログを表示します。
 */
export const Calendar: React.FC = () => {
  const [events, setEvents] = useState<any[]>([]);
  const [currentMonth, setCurrentMonth] = useState<string>(dayjs().format('YYYY-MM'));
  const [selectedLog, setSelectedLog] = useState<LogDetailResponse | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const calendarRef = useRef<any>(null);

  // カレンダーイベントの取得
  const loadCalendarEvents = async (month: string) => {
    try {
      const data = await demoApi.getDemoCalendarEvents(month);
      // Googleカレンダー風の色合いにマッピング
      const mapped = data.map((ev: DemoCalendarListResponse.CalendarEvent) => {
        return {
          id: ev.date,
          title: ev.title || (ev.holiday ? '🌴 休暇' : '💻 稼働'),
          start: ev.date,
          allDay: true,
          // Googleカレンダー風のソリッドなパステルマテリアルカラー
          backgroundColor: ev.holiday ? '#e67c73' : '#1a73e8', // 休日: フラミンゴピンク, 稼働: Googleブルー
          textColor: '#ffffff',
          borderColor: ev.holiday ? '#e67c73' : '#1a73e8',
          extendedProps: { ...ev }
        };
      });
      setEvents(mapped);
    } catch (err) {
      console.error('Failed to load calendar events:', err);
      setEvents([]);
    }
  };

  useEffect(() => {
    loadCalendarEvents(currentMonth);
  }, [currentMonth]);

  // 日付または予定をクリックしたときの詳細取得
  const handleDateClick = async (dateStr: string) => {
    setLoadingDetail(true);
    setSelectedLog(null);
    setDetailOpen(true);
    try {
      const response = await logApi.getLogDetails(dateStr);
      setSelectedLog(response);
    } catch (err: any) {
      console.log('No log details for this day:', err);
      setSelectedLog(null);
    } finally {
      setLoadingDetail(false);
    }
  };

  return (
    <Box sx={{ py: 2 }}>
      {/* ページタイトル */}
      <Box sx={{ mb: 4, display: 'flex', alignItems: 'center', gap: 1.5 }}>
        <CalendarIcon size={28} color="#1a73e8" />
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800 }}>
            カレンダー
          </Typography>
          <Typography variant="body2" sx={{ color: '#718096' }}>
            デモアカウントに登録された日報をカレンダー形式で表示しています。
          </Typography>
        </Box>
      </Box>

      {/* カレンダー表示 */}
      <Box
        sx={{
          backgroundColor: '#ffffff',
          p: 3,
          borderRadius: '12px',
          border: '1px solid #dadce0',
          boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)',
          '& .fc': {
            '--fc-border-color': '#dadce0',
            '--fc-page-bg-color': '#ffffff',
            fontFamily: '"Google Sans", Roboto, "Segoe UI", Arial, sans-serif',
          },
          '& .fc-header-toolbar': {
            mb: '24px !important',
            display: 'flex',
            alignItems: 'center',
          },
          '& .fc-toolbar-title': {
            fontSize: '22px !important',
            fontWeight: '400 !important',
            color: '#3c4043',
            ml: 2,
          },
          '& .fc-button-primary': {
            backgroundColor: '#ffffff !important',
            border: '1px solid #dadce0 !important',
            color: '#3c4043 !important',
            fontWeight: '500 !important',
            fontSize: '14px !important',
            borderRadius: '4px !important',
            boxShadow: 'none !important',
            backgroundImage: 'none !important',
            textTransform: 'none !important',
            padding: '6px 12px !important',
            transition: 'background-color 0.15s, border-color 0.15s',
            '&:hover': {
              backgroundColor: '#f1f3f4 !important',
              borderColor: '#d2d4d7 !important',
            },
            '&:active, &:focus': {
              backgroundColor: '#e8eaed !important',
            },
            '&.fc-button-active': {
              backgroundColor: '#e8f0fe !important',
              color: '#1a73e8 !important',
              borderColor: '#d2e3fc !important',
            }
          },
          // ナビゲーションの矢印ボタン
          '& .fc-prev-button, & .fc-next-button': {
            padding: '5px 8px !important',
            borderRadius: '50% !important', // 丸型ボタンに
            border: 'none !important',
            '&:hover': {
              backgroundColor: '#f1f3f4 !important',
            }
          },
          // 曜日ヘッダーセル
          '& .fc-col-header-cell': {
            borderBottom: 'none',
            pb: 1,
          },
          '& .fc-col-header-cell-cushion': {
            fontSize: '11px',
            fontWeight: '500',
            color: '#70757a',
            textTransform: 'uppercase',
            textDecoration: 'none !important',
          },
          // 各日付セル
          '& .fc-daygrid-day': {
            cursor: 'pointer',
            transition: 'background-color 0.2s',
            '&:hover': {
              backgroundColor: '#f1f3f4',
            }
          },
          // 日付の数字
          '& .fc-daygrid-day-number': {
            fontFamily: '"Google Sans", Roboto, sans-serif',
            fontSize: '12px',
            fontWeight: '500',
            color: '#3c4043',
            padding: '8px 8px 0 0 !important',
            textDecoration: 'none !important',
            display: 'inline-block',
            textAlign: 'center',
            lineHeight: '24px',
            minWidth: '24px',
            height: '24px',
          },
          // 今日の日付セルの強調
          '& .fc-day-today': {
            backgroundColor: 'transparent !important',
            '& .fc-daygrid-day-number': {
              backgroundColor: '#1a73e8 !important',
              color: '#ffffff !important',
              borderRadius: '50% !important',
              marginTop: '4px',
              marginRight: '4px',
              padding: '0 !important',
            }
          },
          // イベントチップ
          '& .fc-event': {
            cursor: 'pointer',
            padding: '2px 8px',
            borderRadius: '4px !important',
            fontSize: '12px',
            fontWeight: '500',
            border: 'none !important',
            boxShadow: 'none !important',
            margin: '2px 4px !important',
          },
          '& .fc-event-title': {
            fontWeight: '500',
          }
        }}
      >
        <FullCalendar
          ref={calendarRef}
          plugins={[dayGridPlugin, interactionPlugin]}
          initialView="dayGridMonth"
          locale="ja"
          events={events}
          height="auto"
          datesSet={(dateInfo) => {
            // 表示月が切り替わった時にAPIを叩く
            const targetMonth = dayjs(dateInfo.view.currentStart).format('YYYY-MM');
            setCurrentMonth(targetMonth);
          }}
          dateClick={(info: any) => {
            handleDateClick(info.dateStr);
          }}
          eventClick={(info: any) => {
            handleDateClick(info.event.id);
          }}
        />
      </Box>

      {/* 詳細ダイアログ (モーダル) */}
      <LogDetailDialog
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        log={selectedLog}
        loading={loadingDetail}
        showApiInfo
        title="詳細情報"
      />
    </Box>
  );
};
