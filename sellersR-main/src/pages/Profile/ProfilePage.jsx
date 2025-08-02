import { useState, useEffect } from 'react';
import axios from 'axios';
import './ProfilePage.css';

const ProfilePage = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [activeTab, setActiveTab] = useState('profile');
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    wildberriesApiKey: ''
  });
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [errors, setErrors] = useState({});
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    fetchUserInfo();
  }, []);

  const fetchUserInfo = async () => {
    try {
      setLoading(true);
      
      // Проверяем наличие токена
      const token = localStorage.getItem('token');
      if (!token) {
        setErrors({ general: 'Для доступа к профилю необходимо авторизоваться' });
        setLoading(false);
        return;
      }

      // Получаем данные пользователя из localStorage сначала
      const localUserData = localStorage.getItem('user');
      if (localUserData) {
        try {
          const parsedUser = JSON.parse(localUserData);
          setUser(parsedUser);
          setFormData({
            firstName: parsedUser.firstName || '',
            lastName: parsedUser.lastName || '',
            phoneNumber: parsedUser.phoneNumber || '',
            wildberriesApiKey: parsedUser.hasApiKey ? '••••••••••••••••' : ''
          });
        } catch (e) {
          console.error('Error parsing user data:', e);
        }
      }

      // Делаем запрос к API для получения актуальных данных
      const response = await axios.get('/api/auth/user-info', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
      
      if (response.data.success) {
        const userData = response.data.user;
        setUser(userData);
        setFormData({
          firstName: userData.firstName || '',
          lastName: userData.lastName || '',
          phoneNumber: userData.phoneNumber || '',
          wildberriesApiKey: userData.hasApiKey ? '••••••••••••••••' : ''
        });

        // Обновляем данные в localStorage
        localStorage.setItem('user', JSON.stringify(userData));
      }
    } catch (error) {
      console.error('Error fetching user info:', error);
      // Если запрос не удался, используем данные из localStorage
      const localUserData = localStorage.getItem('user');
      if (localUserData) {
        try {
          const parsedUser = JSON.parse(localUserData);
          setUser(parsedUser);
          setFormData({
            firstName: parsedUser.firstName || '',
            lastName: parsedUser.lastName || '',
            phoneNumber: parsedUser.phoneNumber || '',
            wildberriesApiKey: parsedUser.hasApiKey ? '••••••••••••••••' : ''
          });
        } catch (e) {
          console.error('Error parsing cached user data:', e);
        }
      }
      setErrors({ general: 'Ошибка загрузки данных пользователя' });
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Очищаем ошибки при изменении поля
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({
      ...prev,
      [name]: value
    }));
    
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.firstName.trim()) {
      newErrors.firstName = 'Имя обязательно для заполнения';
    }
    
    if (!formData.lastName.trim()) {
      newErrors.lastName = 'Фамилия обязательна для заполнения';
    }
    
    if (formData.phoneNumber && !/^\+?[\d\s\-\(\)]+$/.test(formData.phoneNumber)) {
      newErrors.phoneNumber = 'Некорректный формат телефона';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validatePassword = () => {
    const newErrors = {};
    
    if (!passwordData.currentPassword) {
      newErrors.currentPassword = 'Введите текущий пароль';
    }
    
    if (!passwordData.newPassword) {
      newErrors.newPassword = 'Введите новый пароль';
    } else if (passwordData.newPassword.length < 6) {
      newErrors.newPassword = 'Пароль должен содержать минимум 6 символов';
    }
    
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      newErrors.confirmPassword = 'Пароли не совпадают';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleProfileUpdate = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    try {
      setSaving(true);
      setSuccessMessage('');
      
      // Получаем токен из localStorage
      const token = localStorage.getItem('token');
      if (!token) {
        setErrors({ general: 'Необходима авторизация' });
        setSaving(false);
        return;
      }
      
      const response = await axios.post('/api/auth/update-profile', {
        firstName: formData.firstName,
        lastName: formData.lastName,
        phoneNumber: formData.phoneNumber
      }, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.data.success) {
        setSuccessMessage('Профиль успешно обновлен');
        await fetchUserInfo(); // Обновляем данные
      } else {
        setErrors({ general: response.data.message || 'Ошибка обновления профиля' });
      }
    } catch (error) {
      console.error('Error updating profile:', error);
      setErrors({ general: 'Ошибка обновления профиля' });
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordUpdate = async (e) => {
    e.preventDefault();
    
    if (!validatePassword()) return;
    
    try {
      setSaving(true);
      setSuccessMessage('');
      
      // Получаем токен из localStorage
      const token = localStorage.getItem('token');
      if (!token) {
        setErrors({ general: 'Необходима авторизация' });
        setSaving(false);
        return;
      }
      
      const response = await axios.post('/api/auth/change-password', {
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword
      }, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.data.success) {
        setSuccessMessage('Пароль успешно изменен');
        setPasswordData({
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        });
      } else {
        setErrors({ general: response.data.message || 'Ошибка изменения пароля' });
      }
    } catch (error) {
      console.error('Error changing password:', error);
      setErrors({ general: 'Ошибка изменения пароля' });
    } finally {
      setSaving(false);
    }
  };

  const handleApiKeyUpdate = async () => {
    try {
      setSaving(true);
      setSuccessMessage('');
      
      // Получаем токен из localStorage
      const token = localStorage.getItem('token');
      if (!token) {
        setErrors({ wildberriesApiKey: 'Необходима авторизация' });
        setSaving(false);
        return;
      }
      
      // Проверяем наличие email
      if (!user?.email) {
        setErrors({ wildberriesApiKey: 'Email пользователя не найден' });
        setSaving(false);
        return;
      }
      
      const response = await axios.post(`/api/auth/set-api-key?email=${encodeURIComponent(user.email)}`, {
        apiKey: formData.wildberriesApiKey
      }, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.data.success) {
        setSuccessMessage('API ключ успешно обновлен');
        await fetchUserInfo();
      } else {
        setErrors({ wildberriesApiKey: response.data.message || 'Ошибка обновления API ключа' });
      }
    } catch (error) {
      console.error('Error updating API key:', error);
      setErrors({ wildberriesApiKey: 'Ошибка обновления API ключа' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="profile-page">
        <div className="container">
          <div className="loading-screen">
            <div className="loading-spinner"></div>
            <p>Загрузка профиля...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-page">
      <div className="container">
        <div className="profile-header">
          <div className="profile-avatar">
            <div className="avatar-circle">
              {user?.firstName?.charAt(0) || user?.email?.charAt(0) || 'U'}
            </div>
            <div className="avatar-info">
              <h1 className="profile-name">
                {user?.firstName} {user?.lastName} 
                {user?.isVerified && <span className="verified-badge">✅</span>}
              </h1>
              <p className="profile-email">{user?.email}</p>
              <div className="profile-stats">
                <div className="stat">
                  <span className="stat-label">Подписка:</span>
                  <span className={`stat-value ${user?.hasSubscription ? 'active' : 'inactive'}`}>
                    {user?.hasSubscription ? 'Активна' : 'Неактивна'}
                  </span>
                </div>
                <div className="stat">
                  <span className="stat-label">API ключ:</span>
                  <span className={`stat-value ${user?.hasApiKey ? 'active' : 'inactive'}`}>
                    {user?.hasApiKey ? 'Установлен' : 'Не установлен'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="profile-content">
          <div className="profile-tabs">
            <button
              className={`tab-button ${activeTab === 'profile' ? 'tab-button-active' : ''}`}
              onClick={() => setActiveTab('profile')}
            >
              <span className="tab-icon">👤</span>
              Профиль
            </button>
            <button
              className={`tab-button ${activeTab === 'security' ? 'tab-button-active' : ''}`}
              onClick={() => setActiveTab('security')}
            >
              <span className="tab-icon">🔒</span>
              Безопасность
            </button>
            <button
              className={`tab-button ${activeTab === 'api' ? 'tab-button-active' : ''}`}
              onClick={() => setActiveTab('api')}
            >
              <span className="tab-icon">🔑</span>
              API ключ
            </button>
          </div>

          <div className="profile-panels">
            {/* Панель профиля */}
            {activeTab === 'profile' && (
              <div className="profile-panel">
                <div className="panel-header">
                  <h2 className="panel-title">Персональная информация</h2>
                  <p className="panel-description">
                    Обновите свою личную информацию и контактные данные
                  </p>
                </div>

                <form onSubmit={handleProfileUpdate} className="profile-form">
                  <div className="form-row">
                    <div className="form-group">
                      <label className="form-label" htmlFor="firstName">
                        Имя *
                      </label>
                      <input
                        type="text"
                        id="firstName"
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleInputChange}
                        className={`form-input ${errors.firstName ? 'form-input-error' : ''}`}
                        placeholder="Введите ваше имя"
                      />
                      {errors.firstName && (
                        <div className="form-error">{errors.firstName}</div>
                      )}
                    </div>

                    <div className="form-group">
                      <label className="form-label" htmlFor="lastName">
                        Фамилия *
                      </label>
                      <input
                        type="text"
                        id="lastName"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleInputChange}
                        className={`form-input ${errors.lastName ? 'form-input-error' : ''}`}
                        placeholder="Введите вашу фамилию"
                      />
                      {errors.lastName && (
                        <div className="form-error">{errors.lastName}</div>
                      )}
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="phoneNumber">
                      Номер телефона
                    </label>
                    <input
                      type="tel"
                      id="phoneNumber"
                      name="phoneNumber"
                      value={formData.phoneNumber}
                      onChange={handleInputChange}
                      className={`form-input ${errors.phoneNumber ? 'form-input-error' : ''}`}
                      placeholder="+7 (999) 123-45-67"
                    />
                    {errors.phoneNumber && (
                      <div className="form-error">{errors.phoneNumber}</div>
                    )}
                  </div>

                  <div className="form-actions">
                    <button
                      type="submit"
                      className="btn btn-primary"
                      disabled={saving}
                    >
                      {saving ? (
                        <>
                          <span className="loading-spinner-small"></span>
                          Сохранение...
                        </>
                      ) : (
                        <>
                          <span>💾</span>
                          Сохранить изменения
                        </>
                      )}
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Панель безопасности */}
            {activeTab === 'security' && (
              <div className="profile-panel">
                <div className="panel-header">
                  <h2 className="panel-title">Безопасность аккаунта</h2>
                  <p className="panel-description">
                    Измените пароль для защиты вашего аккаунта
                  </p>
                </div>

                <form onSubmit={handlePasswordUpdate} className="profile-form">
                  <div className="form-group">
                    <label className="form-label" htmlFor="currentPassword">
                      Текущий пароль *
                    </label>
                    <input
                      type="password"
                      id="currentPassword"
                      name="currentPassword"
                      value={passwordData.currentPassword}
                      onChange={handlePasswordChange}
                      className={`form-input ${errors.currentPassword ? 'form-input-error' : ''}`}
                      placeholder="Введите текущий пароль"
                    />
                    {errors.currentPassword && (
                      <div className="form-error">{errors.currentPassword}</div>
                    )}
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="newPassword">
                      Новый пароль *
                    </label>
                    <input
                      type="password"
                      id="newPassword"
                      name="newPassword"
                      value={passwordData.newPassword}
                      onChange={handlePasswordChange}
                      className={`form-input ${errors.newPassword ? 'form-input-error' : ''}`}
                      placeholder="Введите новый пароль (минимум 6 символов)"
                    />
                    {errors.newPassword && (
                      <div className="form-error">{errors.newPassword}</div>
                    )}
                  </div>

                  <div className="form-group">
                    <label className="form-label" htmlFor="confirmPassword">
                      Подтвердите новый пароль *
                    </label>
                    <input
                      type="password"
                      id="confirmPassword"
                      name="confirmPassword"
                      value={passwordData.confirmPassword}
                      onChange={handlePasswordChange}
                      className={`form-input ${errors.confirmPassword ? 'form-input-error' : ''}`}
                      placeholder="Повторите новый пароль"
                    />
                    {errors.confirmPassword && (
                      <div className="form-error">{errors.confirmPassword}</div>
                    )}
                  </div>

                  <div className="form-actions">
                    <button
                      type="submit"
                      className="btn btn-primary"
                      disabled={saving}
                    >
                      {saving ? (
                        <>
                          <span className="loading-spinner-small"></span>
                          Изменение...
                        </>
                      ) : (
                        <>
                          <span>🔐</span>
                          Изменить пароль
                        </>
                      )}
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Панель API ключа */}
            {activeTab === 'api' && (
              <div className="profile-panel">
                <div className="panel-header">
                  <h2 className="panel-title">API ключ Wildberries</h2>
                  <p className="panel-description">
                    Установите API ключ для синхронизации данных с Wildberries
                  </p>
                </div>

                <div className="api-info">
                  <div className="info-card">
                    <div className="info-icon">🔑</div>
                    <div className="info-content">
                      <h4>Как получить API ключ?</h4>
                      <ol>
                        <li>Войдите в личный кабинет продавца Wildberries</li>
                        <li>Перейдите в раздел "Настройки" → "API"</li>
                        <li>Создайте новый токен с правами на чтение статистики</li>
                        <li>Скопируйте полученный ключ и вставьте его ниже</li>
                      </ol>
                    </div>
                  </div>
                </div>

                <div className="profile-form">
                  <div className="form-group">
                    <label className="form-label" htmlFor="wildberriesApiKey">
                      API ключ Wildberries
                    </label>
                    <div className="api-input-group">
                      <input
                        type={user?.hasApiKey && formData.wildberriesApiKey.includes('•') ? 'password' : 'text'}
                        id="wildberriesApiKey"
                        name="wildberriesApiKey"
                        value={formData.wildberriesApiKey}
                        onChange={handleInputChange}
                        className={`form-input ${errors.wildberriesApiKey ? 'form-input-error' : ''}`}
                        placeholder="Вставьте ваш API ключ"
                      />
                      <button
                        type="button"
                        onClick={handleApiKeyUpdate}
                        className="btn btn-primary"
                        disabled={saving || !formData.wildberriesApiKey}
                      >
                        {saving ? (
                          <span className="loading-spinner-small"></span>
                        ) : (
                          <span>💾</span>
                        )}
                        {user?.hasApiKey ? 'Обновить' : 'Сохранить'}
                      </button>
                    </div>
                    {errors.wildberriesApiKey && (
                      <div className="form-error">{errors.wildberriesApiKey}</div>
                    )}
                    
                    {user?.hasApiKey && (
                      <div className="api-status">
                        <span className="status-icon">✅</span>
                        API ключ установлен и активен
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Сообщения */}
        {successMessage && (
          <div className="success-message">
            <span className="success-icon">✅</span>
            {successMessage}
          </div>
        )}

        {errors.general && (
          <div className="error-message">
            <span className="error-icon">❌</span>
            {errors.general}
          </div>
        )}
      </div>
    </div>
  );
};

export default ProfilePage;