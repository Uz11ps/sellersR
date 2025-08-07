import { useState, useEffect } from 'react';
import axios from 'axios';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';

// Layout components
import Header from './components/Layout/Header';

// Page components
import HomePage from './pages/Home/HomePage';
import AnalyticsPage from './pages/Analytics/AnalyticsPage';
import ProfilePage from './pages/Profile/ProfilePage';
import SubscriptionPage from './pages/Subscription/SubscriptionPage';
import AboutPage from './pages/About/AboutPage';

// Set up axios defaults
axios.defaults.baseURL = 'http://localhost:8080';

// Настройка axios interceptors для работы с JWT
axios.interceptors.request.use(
  config => {
    // Убеждаемся что headers существует
    if (!config.headers) {
      config.headers = {};
    }
    
    const token = localStorage.getItem('token');
    console.log('🔍 Interceptor triggered for:', config.url);
    console.log('🔍 Token exists:', token ? 'YES' : 'NO');
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
      console.log('🔐 Added auth header to request:', config.url);
      console.log('🔍 Final headers being sent:', JSON.stringify(config.headers, null, 2));
      
      // Дополнительная проверка для аналитики
      if (config.url && config.url.includes('/analytics/')) {
        console.log('🎯 ANALYTICS REQUEST - Authorization header:', config.headers['Authorization'] ? 'SET' : 'MISSING');
        console.log('🎯 ANALYTICS REQUEST - Token preview:', token.substring(0, 50) + '...');
      }
    } else {
      console.log('⚠️ No token found for request:', config.url);
    }
    return config;
  },
  error => {
    console.error('❌ Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Обработка ошибок аутентификации
axios.interceptors.response.use(
  response => {
    console.log('Response from:', response.config.url, 'Status:', response.status);
    
    // Проверяем, содержит ли ответ информацию о истекшем токене
    if (response.data && response.data.tokenExpired === true) {
      console.log('Token expired detected in response');
      
      if (response.data.newToken) {
        console.log('Updating token with new one from response');
        localStorage.setItem('token', response.data.newToken);
        axios.defaults.headers.common['Authorization'] = `Bearer ${response.data.newToken}`;
        
        if (!response.config.url.includes('/subscription/info')) {
          showNotification('Ваша сессия была обновлена', 'success');
        }
      } else {
        const userData = JSON.parse(localStorage.getItem('user') || '{}');
        
        if (userData.email && userData._savedPassword) {
          console.log('Attempting to re-login with saved credentials');
          
          const axiosInstance = axios.create({
            baseURL: 'http://localhost:8080'
          });
          
          axiosInstance.post('/api/auth/login', {
            email: userData.email,
            password: userData._savedPassword
          })
          .then(loginResponse => {
            if (loginResponse.data.success) {
              console.log('Re-login successful, updating token');
              
              localStorage.setItem('token', loginResponse.data.token);
              localStorage.setItem('user', JSON.stringify({
                ...loginResponse.data.user,
                _savedPassword: userData._savedPassword
              }));
              
              axios.defaults.headers.common['Authorization'] = `Bearer ${loginResponse.data.token}`;
              showNotification('Ваша сессия была обновлена', 'success');
            } else {
              console.log('Re-login failed, redirecting to login page');
              clearAuthData();
            }
          })
          .catch(loginError => {
            console.error('Error during re-login:', loginError);
            clearAuthData();
          });
        } else {
          console.log('No saved credentials, redirecting to login');
          clearAuthData();
        }
      }
    }
    
    return response;
  },
  error => {
    if (error.response) {
      console.error('Response error:', error.response.status, error.response.data);
      
      if (error.response.status === 401) {
        console.log('Unauthorized access detected');
        
        if (!error.config.url.includes('/auth/login') && !error.config.url.includes('/auth/register')) {
          console.log('Token might be invalid, attempting to refresh session');
          
          const userData = JSON.parse(localStorage.getItem('user') || '{}');
          
          if (userData.email && userData._savedPassword) {
            console.log('Attempting to re-login with saved credentials');
            
            const axiosInstance = axios.create({
              baseURL: 'http://localhost:8080'
            });
            
            return axiosInstance.post('/api/auth/login', {
              email: userData.email,
              password: userData._savedPassword
            })
            .then(response => {
              if (response.data.success) {
                console.log('Re-login successful, updating token');
                
                localStorage.setItem('token', response.data.token);
                localStorage.setItem('user', JSON.stringify({
                  ...response.data.user,
                  _savedPassword: userData._savedPassword
                }));
                
                axios.defaults.headers.common['Authorization'] = `Bearer ${response.data.token}`;
                
                error.config.headers.Authorization = `Bearer ${response.data.token}`;
                
                // Убираем добавление timestamp параметра - это вызывает проблемы с многократными _t= параметрами
                // error.config.url остается без изменений
                
                return axios(error.config);
              } else {
                return Promise.reject(error);
              }
            })
            .catch(reLoginError => {
              console.error('Re-login failed:', reLoginError);
              return Promise.reject(error);
            });
          } else {
            console.log('No saved credentials, redirecting to login page');
            clearAuthData();
            
            if (!window.location.pathname.includes('/login') && !window.location.pathname === '/') {
              showNotification('Ваша сессия истекла. Выполните вход снова.', 'error');
              setTimeout(() => {
                window.location.href = '/';
              }, 2000);
            } else {
              window.location.href = '/';
            }
            
            return Promise.reject(error);
          }
        }
      }
    }
    return Promise.reject(error);
  }
);

// Утилитарные функции
const clearAuthData = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.href = '/';
};

const showNotification = (message, type = 'info') => {
  const notification = document.createElement('div');
  notification.textContent = message;
  notification.className = `notification notification-${type}`;
  notification.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 12px 20px;
    border-radius: 8px;
    color: white;
    font-weight: 500;
    z-index: 10000;
    animation: slideInRight 0.3s ease-out;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(10px);
    ${type === 'success' ? 'background: linear-gradient(135deg, #48DD00, #52A529);' : 
      type === 'error' ? 'background: linear-gradient(135deg, #FF4757, #FF3838);' : 
      'background: linear-gradient(135deg, #9F3ED5, #AD66D5);'}
  `;
  
  document.body.appendChild(notification);
  
  setTimeout(() => {
    notification.style.animation = 'slideOutRight 0.3s ease-out';
    setTimeout(() => {
      if (document.body.contains(notification)) {
        document.body.removeChild(notification);
      }
    }, 300);
  }, 3000);
};

function App() {
  const [isLoading, setIsLoading] = useState(true);

  // Проверка валидности токена при загрузке приложения
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      console.log('Token found in localStorage');
      
      axios.get('/api/auth/check-token')
        .then(response => {
          console.log('Token validation response:', response.data);
          
          if (response.data && response.data.tokenExpired === true) {
            console.log('Token expired detected in initial check');
            
            if (response.data.newToken) {
              console.log('Updating token with new one from response');
              localStorage.setItem('token', response.data.newToken);
            } else {
              const userData = JSON.parse(localStorage.getItem('user') || '{}');
              if (userData.email && userData._savedPassword) {
                console.log('Attempting to re-login with saved credentials');
                
                axios.post('/api/auth/login', {
                  email: userData.email,
                  password: userData._savedPassword
                })
                .then(loginResponse => {
                  if (loginResponse.data.success) {
                    console.log('Re-login successful, updating token');
                    
                    localStorage.setItem('token', loginResponse.data.token);
                    localStorage.setItem('user', JSON.stringify({
                      ...loginResponse.data.user,
                      _savedPassword: userData._savedPassword
                    }));
                  }
                })
                .catch(loginError => {
                  console.error('Re-login failed:', loginError);
                  clearAuthData();
                });
              }
            }
          }
        })
        .catch(error => {
          console.error('Token validation error:', error);
          
          if (error.response && error.response.status === 401) {
            const userData = JSON.parse(localStorage.getItem('user') || '{}');
            if (!userData._savedPassword) {
              console.log('Token is invalid and no saved credentials, clearing localStorage');
              localStorage.removeItem('token');
              localStorage.removeItem('user');
            }
          }
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else {
      setIsLoading(false);
    }
  }, []);

  if (isLoading) {
    return (
      <div className="app-loading">
        <div className="loading-spinner"></div>
        <p>Загрузка SellLab...</p>
      </div>
    );
  }

  return (
    <Router>
      <div className="app">
        <Header />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/analytics" element={<AnalyticsPage />} />
            <Route path="/subscription" element={<SubscriptionPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/about" element={<AboutPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;