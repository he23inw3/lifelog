const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// Paths
const workspaceRoot = path.join(__dirname, '..');
const deployMdPath = path.join(workspaceRoot, 'DEPLOY.md');
const tempHtmlPath = path.join(workspaceRoot, 'temp_deploy.html');
const outputPdfPath = path.join(workspaceRoot, 'app', 'lifelog-demo', 'public', 'production-guide.pdf');

function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function parseInline(text) {
  let res = escapeHtml(text);
  // Bold
  res = res.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  // Italic
  res = res.replace(/\*([^*]+)\*/g, '<em>$1</em>');
  // Code
  res = res.replace(/`([^`]+)`/g, '<code>$1</code>');
  // Links
  res = res.replace(/\[([^\]]+)\]\(([^)]+)\)/g, (match, linkText, url) => {
    return `<a href="${url}" target="_blank">${linkText}</a>`;
  });
  return res;
}

function mdToHtml(md) {
  const lines = md.replace(/\r\n/g, '\n').split('\n');
  let html = '';
  let inList = false;
  let listType = ''; // 'ul' or 'ol'
  let inCode = false;
  let codeContent = [];
  let codeLang = '';
  let inAlert = false;
  let alertType = ''; // 'NOTE', 'TIP', etc.
  let alertContent = [];

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    // Code blocks
    if (line.startsWith('```')) {
      if (inCode) {
        inCode = false;
        html += `<pre><code class="language-${codeLang}">${escapeHtml(codeContent.join('\n'))}</code></pre>\n`;
        codeContent = [];
      } else {
        inCode = true;
        codeLang = line.slice(3).trim();
      }
      continue;
    }

    if (inCode) {
      codeContent.push(line);
      continue;
    }

    // Alerts (GitHub style: > [!NOTE])
    if (line.startsWith('> [!')) {
      if (inList) {
        html += `</${listType}>\n`;
        inList = false;
      }
      inAlert = true;
      const match = line.match(/> \[!(.*?)\]/);
      alertType = match ? match[1].toLowerCase() : 'note';
      alertContent = [];
      continue;
    }

    if (inAlert) {
      if (line.startsWith('>')) {
        let contentLine = line.slice(1).trim();
        alertContent.push(contentLine);
        continue;
      } else {
        inAlert = false;
        html += `<div class="alert alert-${alertType}">${mdToHtml(alertContent.join('\n'))}</div>\n`;
      }
    }

    // Unordered lists
    if (line.startsWith('* ') || line.startsWith('- ')) {
      if (!inList || listType !== 'ul') {
        if (inList) html += `</${listType}>\n`;
        inList = true;
        listType = 'ul';
        html += '<ul>\n';
      }
      html += `<li>${parseInline(line.slice(2))}</li>\n`;
      continue;
    }

    // Ordered lists
    const olMatch = line.match(/^(\d+)\.\s(.*)$/);
    if (olMatch) {
      if (!inList || listType !== 'ol') {
        if (inList) html += `</${listType}>\n`;
        inList = true;
        listType = 'ol';
        html += '<ol>\n';
      }
      html += `<li>${parseInline(olMatch[2])}</li>\n`;
      continue;
    }

    // Close lists on empty lines or other constructs
    if (inList && (line.trim() === '' || line.startsWith('#') || line.startsWith('>'))) {
      html += `</${listType}>\n`;
      inList = false;
    }

    // Headings
    if (line.startsWith('# ')) {
      html += `<h1>${parseInline(line.slice(2))}</h1>\n`;
    } else if (line.startsWith('## ')) {
      html += `<h2>${parseInline(line.slice(3))}</h2>\n`;
    } else if (line.startsWith('### ')) {
      html += `<h3>${parseInline(line.slice(4))}</h3>\n`;
    } else if (line.startsWith('#### ')) {
      html += `<h4>${parseInline(line.slice(5))}</h4>\n`;
    } else if (line.trim() === '---') {
      html += '<hr />\n';
    } else if (line.trim() !== '') {
      html += `<p>${parseInline(line)}</p>\n`;
    }
  }

  if (inList) {
    html += `</${listType}>\n`;
  }
  if (inAlert) {
    html += `<div class="alert alert-${alertType}">${mdToHtml(alertContent.join('\n'))}</div>\n`;
  }

  return html;
}

try {
  console.log('Reading DEPLOY.md...');
  const md = fs.readFileSync(deployMdPath, 'utf8');

  console.log('Converting Markdown to HTML...');
  const bodyHtml = mdToHtml(md);

  const fullHtml = `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>LifeLog Production Deployment Guide</title>
  <style>
    body {
      font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
      line-height: 1.6;
      color: #1a202c;
      max-width: 800px;
      margin: 0 auto;
      padding: 40px 30px;
    }
    h1, h2, h3, h4 {
      color: #111827;
      font-weight: 700;
      margin-top: 28px;
      margin-bottom: 12px;
      page-break-after: avoid;
    }
    h1 {
      font-size: 2.2rem;
      border-bottom: 2px solid #e5e7eb;
      padding-bottom: 8px;
      margin-top: 0;
    }
    h2 {
      font-size: 1.6rem;
      border-bottom: 1px solid #e5e7eb;
      padding-bottom: 6px;
      margin-top: 36px;
    }
    h3 {
      font-size: 1.25rem;
    }
    p {
      margin-bottom: 16px;
    }
    ul, ol {
      margin-bottom: 16px;
      padding-left: 24px;
    }
    li {
      margin-bottom: 6px;
    }
    code {
      font-family: Consolas, Monaco, "Andale Mono", monospace;
      background-color: #f3f4f6;
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 0.9em;
    }
    pre {
      background-color: #f9fafb;
      border: 1px solid #e5e7eb;
      border-radius: 8px;
      padding: 16px;
      overflow-x: auto;
      margin-bottom: 16px;
      page-break-inside: avoid;
    }
    pre code {
      background-color: transparent;
      padding: 0;
      border-radius: 0;
      font-size: 0.9em;
    }
    .alert {
      padding: 16px;
      margin: 20px 0;
      border-radius: 8px;
      border-left: 4px solid;
      page-break-inside: avoid;
    }
    .alert-note {
      background-color: #f0fdf4;
      border-color: #22c55e;
      color: #14532d;
    }
    .alert-tip {
      background-color: #eff6ff;
      border-color: #3b82f6;
      color: #1e3a8a;
    }
    .alert-important {
      background-color: #fffbeb;
      border-color: #d97706;
      color: #78350f;
    }
    .alert-warning {
      background-color: #fef2f2;
      border-color: #ef4444;
      color: #7f1d1d;
    }
    a {
      color: #2563eb;
      text-decoration: none;
    }
    a:hover {
      text-decoration: underline;
    }
    hr {
      border: 0;
      border-top: 1px solid #e5e7eb;
      margin: 24px 0;
    }
    @media print {
      body {
        padding: 20px;
      }
    }
  </style>
</head>
<body>
  ${bodyHtml}
</body>
</html>`;

  fs.writeFileSync(tempHtmlPath, fullHtml, 'utf8');
  console.log('Temporary HTML file created.');

  // Create public dir if it doesn't exist
  const publicDir = path.dirname(outputPdfPath);
  if (!fs.existsSync(publicDir)) {
    fs.mkdirSync(publicDir, { recursive: true });
  }

  // Compile to PDF using Microsoft Edge
  const edgePath = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
  const command = `"${edgePath}" --headless --print-to-pdf="${outputPdfPath}" "${tempHtmlPath}"`;
  
  console.log('Running Microsoft Edge headless to generate PDF...');
  execSync(command);
  console.log(`PDF successfully generated at: ${outputPdfPath}`);

  // Clean up
  fs.unlinkSync(tempHtmlPath);
  console.log('Temporary HTML file removed.');

} catch (err) {
  console.error('Error generating PDF:', err);
  process.exit(1);
}
