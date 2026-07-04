package jp.he23inw3.asset.infrastructure.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class TraceHelperTest {

    @Test
    @DisplayName("アクティブなスパンが存在する場合、そのtraceIdを返すこと")
    void testCurrentTraceId_ActiveSpan() {
        try (MockedStatic<Span> spanMock = mockStatic(Span.class)) {
            Span mockSpan = mock(Span.class);
            SpanContext mockSpanContext = mock(SpanContext.class);

            spanMock.when(Span::current).thenReturn(mockSpan);
            when(mockSpan.getSpanContext()).thenReturn(mockSpanContext);
            when(mockSpanContext.getTraceId()).thenReturn("1234567890abcdef1234567890abcdef");

            String traceId = TraceHelper.currentTraceId();

            assertThat(traceId).isEqualTo("1234567890abcdef1234567890abcdef");
        }
    }

    @Test
    @DisplayName("アクティブなスパンのtraceIdがnullの場合、EMPTY_TRACE_IDを返すこと")
    void testCurrentTraceId_NullTraceId() {
        try (MockedStatic<Span> spanMock = mockStatic(Span.class)) {
            Span mockSpan = mock(Span.class);
            SpanContext mockSpanContext = mock(SpanContext.class);

            spanMock.when(Span::current).thenReturn(mockSpan);
            when(mockSpan.getSpanContext()).thenReturn(mockSpanContext);
            when(mockSpanContext.getTraceId()).thenReturn(null);

            String traceId = TraceHelper.currentTraceId();

            assertThat(traceId).isEqualTo(TraceHelper.EMPTY_TRACE_ID);
        }
    }
}
