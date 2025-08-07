import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import axios from 'axios';

// Настройка axios для автоматического добавления токена авторизации
axios.defaults.baseURL = 'http://localhost:8080';

// Interceptors определены в App.jsx - здесь убираем дублирование

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);