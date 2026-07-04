import React, { useState, useRef, useEffect } from 'react';
import { Box, Paper, Avatar, CircularProgress, Typography, useTheme, useMediaQuery } from '@mui/material';
import { AlertCircle } from 'lucide-react';
import { logApi } from '../api/logApi';
import type { LogDetailResponse, Message } from '../types';
import { useSnackbar } from 'notistack';
import dayjs from 'dayjs';

// 子コンポーネントのインポート
import { SlackSidebar } from '../components/home/SlackSidebar';
import { ChatHeader } from '../components/home/ChatHeader';
import { MessageItem } from '../components/home/MessageItem';
import { ChatInputForm } from '../components/home/ChatInputForm';

export const Home: React.FC = () => {
  const { enqueueSnackbar } = useSnackbar();
  const [loading, setLoading] = useState(false);

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm')); // 600px未満をモバイルと判定
  const [sidebarOpen, setSidebarOpen] = useState(!isMobile);

  // ウィンドウのリサイズに応じて開閉状態を自動追従させる
  useEffect(() => {
    setSidebarOpen(!isMobile);
  }, [isMobile]);

  // 対話ログの状態管理
  const [messages, setMessages] = useState<Message[]>(() => {
    const saved = sessionStorage.getItem('lifelog_demo_messages');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        console.error('Failed to parse saved messages', e);
      }
    }
    return [
      {
        id: 'welcome',
        type: 'bot',
        text: 'お疲れ様です！今日（または当月内の任意の稼働日）の作業内容と稼働時間を教えてください。\n\n💡 **ヒント**: 「6月24日は〜」のようにメッセージ内に日付を含めると、今日以外の日付でも自動で判別して登録・更新できます。',
        timestamp: dayjs().format('HH:mm'),
      }
    ];
  });

  // 入力フォームの状態管理
  const [inputText, setInputText] = useState(() => sessionStorage.getItem('lifelog_demo_input_text') || '');
  const [isHoliday, setIsHoliday] = useState(() => sessionStorage.getItem('lifelog_demo_is_holiday') === 'true');
  const [accumulatedText, setAccumulatedText] = useState(() => sessionStorage.getItem('lifelog_demo_accumulated_text') || '');
  const [promptingInput, setPromptingInput] = useState(() => sessionStorage.getItem('lifelog_demo_prompting_input') === 'true');

  const messagesEndRef = useRef<HTMLDivElement>(null);

  // 各ステートが変更された際に sessionStorage に保存してページ遷移時の状態消失を防ぐ
  useEffect(() => {
    sessionStorage.setItem('lifelog_demo_messages', JSON.stringify(messages));
  }, [messages]);

  useEffect(() => {
    sessionStorage.setItem('lifelog_demo_input_text', inputText);
  }, [inputText]);

  useEffect(() => {
    sessionStorage.setItem('lifelog_demo_is_holiday', String(isHoliday));
  }, [isHoliday]);

  useEffect(() => {
    sessionStorage.setItem('lifelog_demo_accumulated_text', accumulatedText);
  }, [accumulatedText]);

  useEffect(() => {
    sessionStorage.setItem('lifelog_demo_prompting_input', String(promptingInput));
  }, [promptingInput]);

  // メッセージの最下部スクロール
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  // 確定ボタン押下時の永続化処理
  const handleConfirmAction = async (msgId: string, result: LogDetailResponse) => {
    setLoading(true);
    try {
      // 確定ボタンが押された際に、実際に日報登録を行う
      await logApi.createLog(result.rawText, result.holiday);

      setMessages(prev => {
        return prev.map(msg => {
          if (msg.id === msgId) {
            return { ...msg, actionClicked: 'confirm' };
          }
          return msg;
        });
      });

      // チャット上に確定の意志表示と Bot からの最終完了通知を追加
      setMessages(prev => [
        ...prev,
        {
          id: `user-confirm-${Date.now()}`,
          type: 'user',
          text: '確定する',
          timestamp: dayjs().format('HH:mm'),
        },
        {
          id: `bot-success-${Date.now()}`,
          type: 'bot',
          text: '確定しました。Google カレンダーへの同期とデータベースへの保存が完了しました！',
          timestamp: dayjs().format('HH:mm'),
          result: result,
        }
      ]);
      enqueueSnackbar('日報の登録を確定しました！', { variant: 'success' });
    } catch (err: any) {
      console.error(err);
      const errMsg = err.response?.data?.message || '日報の確定処理に失敗しました。';
      enqueueSnackbar(errMsg, { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };


  // キャンセルボタン押下時の処理
  const handleCancelAction = (msgId: string) => {
    setMessages(prev => {
      return prev.map(msg => {
        if (msg.id === msgId) {
          return { ...msg, actionClicked: 'cancel' };
        }
        return msg;
      });
    });

    // チャット上にキャンセルメッセージを追加
    setMessages(prev => [
      ...prev,
      {
        id: `user-cancel-${Date.now()}`,
        type: 'user',
        text: 'キャンセルする',
        timestamp: dayjs().format('HH:mm'),
      },
      {
        id: `bot-cancelled-${Date.now()}`,
        type: 'bot',
        text: '日報の登録をキャンセルしました。',
        timestamp: dayjs().format('HH:mm'),
      }
    ]);
    enqueueSnackbar('日報の登録をキャンセルしました。', { variant: 'default' });
  };

  // 送信処理
  const handleSendMessage = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputText.trim() || loading) return;

    const currentInput = inputText;
    setInputText('');

    // 1. ユーザーメッセージをチャットに追加
    const userMsgId = `user-${Date.now()}`;
    const userMessage: Message = {
      id: userMsgId,
      type: 'user',
      text: currentInput,
      timestamp: dayjs().format('HH:mm'),
      isHoliday: isHoliday,
    };
    setMessages(prev => [...prev, userMessage]);
    setLoading(true);

    // 2. テキストマージロジック
    // 聞き返し中であればこれまでの蓄積とマージ、新規であれば現在の入力値
    const targetText = promptingInput ? `${accumulatedText}\n${currentInput}` : currentInput;
    setAccumulatedText(targetText);

    try {
      // 3. API 送信 (登録ではなく解析のみを行う)
      const response = await logApi.analyzeLog(targetText, isHoliday);

      // 登録成功（いったん確定待ちにする）
      setMessages(prev => [
        ...prev,
        {
          id: `bot-pending-${Date.now()}`,
          type: 'bot',
          text: '日報の解析が完了しました。以下の内容でカレンダーおよびデータベースへ登録してもよろしいですか？',
          timestamp: dayjs().format('HH:mm'),
          hasConfirmActions: true,
          pendingResult: response,
          actionClicked: null
        }
      ]);
      setPromptingInput(false);
      setAccumulatedText('');
      setIsHoliday(false);
      enqueueSnackbar('AIによる解析が完了しました。内容を確認して確定してください。', { variant: 'info' });
    } catch (err: any) {
      console.error(err);
      const errMsg = err.response?.data?.message || '登録に失敗しました。バックエンドの接続状況を確認してください。';
      const isValidationError = err.response?.status === 400 && err.response?.data?.errorCode === 'VALIDATION_ERROR';

      if (isValidationError) {
        // バリデーションエラー（聞き返し）
        setPromptingInput(true);
        setMessages(prev => [
          ...prev,
          {
            id: `bot-prompt-${Date.now()}`,
            type: 'bot',
            text: errMsg,
            timestamp: dayjs().format('HH:mm'),
            isError: false,
          }
        ]);
        enqueueSnackbar('入力内容が不足しています。AIの質問に回答してください。', { variant: 'warning' });
      } else {
        // 一般エラー
        setMessages(prev => [
          ...prev,
          {
            id: `bot-error-${Date.now()}`,
            type: 'bot',
            text: `⚠️ エラーが発生しました: ${errMsg}`,
            timestamp: dayjs().format('HH:mm'),
            isError: true,
          }
        ]);
        enqueueSnackbar(errMsg, { variant: 'error' });
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{
      display: 'flex',
      height: 'calc(100vh - 100px)',
      bgcolor: '#f8fafc',
      border: '1px solid #e2e8f0',
      borderRadius: '16px',
      overflow: 'hidden',
      boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)'
    }}>
      {/* 1. Slack風左サイドバー */}
      <SlackSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      {/* 2. Slack風メインチャットエリア */}
      <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1, bgcolor: '#ffffff', height: '100%' }}>
        {/* チャットヘッダー */}
        <ChatHeader sidebarOpen={sidebarOpen} onOpenSidebar={() => setSidebarOpen(true)} />

        {/* チャットメッセージ表示領域 */}
        <Box sx={{ flexGrow: 1, overflowY: 'auto', p: 3, display: 'flex', flexDirection: 'column', gap: 3 }}>

          {/* デモ環境の説明（ピン留め風インフォメーション） */}
          <Paper
            elevation={0}
            sx={{
              p: 2.5,
              bgcolor: '#f0f9ff',
              border: '1px solid #e0f2fe',
              borderRadius: '12px',
              display: 'flex',
              gap: 1.5
            }}
          >
            <AlertCircle size={22} color="#0284c7" style={{ flexShrink: 0, marginTop: '2px' }} />
            <Box>
              <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0369a1', mb: 1, fontSize: '14px' }}>
                【デモ体験環境について】
              </Typography>
              <Typography variant="body2" sx={{ color: '#334155', display: 'block', lineHeight: 1.6, mb: 1.5, fontSize: '13px' }}>
                このチャット画面は、LifeLogのAI解析および聞き返し機能（Slack連携フロー）を手軽に体験するための**デモ画面**です。
              </Typography>

              <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#0369a1', mb: 0.5, fontSize: '13px' }}>
                正式版（本番運用環境）の動作・仕様：
              </Typography>
              <Typography variant="body2" component="div" sx={{ color: '#475569', display: 'block', lineHeight: 1.6, fontSize: '12px', mb: 1.5 }}>
                • **本物のSlackとの連携**: ユーザー自身のSlackワークスペースで Bot と直接対話（DM）して日報を記録します。<br />
                • **リアル同期**: Googleカレンダーの予定が自動で新規追加・上書き同期され、BigQueryに日報データが蓄積されます。<br />
                • **利用者ポータル**: 専用のポータル画面（<code>lifelog-portal</code>）から、アカウント連携設定や自動リマインド時刻の設定・変更が可能です。
              </Typography>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <Typography variant="caption" sx={{ color: '#0284c7', fontWeight: 700 }}>
                  📄 構築・デプロイ方法はこちら：
                </Typography>
                <a
                  href="/production-guide.pdf"
                  target="_blank"
                  rel="noopener noreferrer"
                  style={{
                    color: '#025a87',
                    fontSize: '12px',
                    fontWeight: 700,
                    textDecoration: 'underline'
                  }}
                >
                  本番環境デプロイガイド (PDF)
                </a>
              </Box>

              {/* 開発中ステータス */}
              <Box sx={{ mt: 1.5, p: 1.5, bgcolor: '#fff7ed', border: '1px solid #fed7aa', borderRadius: '8px', display: 'flex', alignItems: 'flex-start', gap: 1 }}>
                <Typography sx={{ fontSize: '16px', flexShrink: 0 }}>🚧</Typography>
                <Typography variant="caption" sx={{ color: '#92400e', lineHeight: 1.6, fontSize: '12px', fontWeight: 600 }}>
                  <strong>現在開発中です。</strong>デモ画面は利用可能ですが、本番 Slack 連携・利用者ポータルなどの機能は現在開発中のため、一般利用はできません。
                </Typography>
              </Box>
            </Box>
          </Paper>

          {/* メッセージログタイムライン */}
          {messages.map((msg) => (
            <MessageItem
              key={msg.id}
              message={msg}
              onConfirm={handleConfirmAction}
              onCancel={handleCancelAction}
            />
          ))}

          {/* Botのタイピングローディング表示 */}
          {loading && (
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
              <Avatar sx={{ bgcolor: '#2bac76', width: 36, height: 36 }}>L</Avatar>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, bgcolor: '#f1f5f9', px: 2, py: 1.2, borderRadius: '12px' }}>
                <CircularProgress size={14} thickness={5} />
                <Typography variant="caption" sx={{ color: '#475569', fontWeight: 600 }}>
                  LifeLog Bot が解析中...
                </Typography>
              </Box>
            </Box>
          )}

          <div ref={messagesEndRef} />
        </Box>

        {/* 3. Slack風メッセージ入力エリア */}
        <ChatInputForm
          inputText={inputText}
          onChangeInput={setInputText}
          isHoliday={isHoliday}
          onChangeHoliday={setIsHoliday}
          onSubmit={handleSendMessage}
          loading={loading}
          promptingInput={promptingInput}
        />
      </Box>
    </Box>
  );
};
