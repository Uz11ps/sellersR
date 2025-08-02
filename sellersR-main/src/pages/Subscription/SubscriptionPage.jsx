import { useState, useEffect } from 'react';
import axios from 'axios';
import './SubscriptionPage.css';

const SubscriptionPage = () => {
  const [user, setUser] = useState(null);
  const [subscription, setSubscription] = useState(null);
  const [availablePlans, setAvailablePlans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState(null);

  // Получаем токен и данные пользователя из localStorage
  const token = localStorage.getItem('token');
  const storedUser = localStorage.getItem('user');

  useEffect(() => {
    // Инициализируем пользователя из localStorage если есть
    if (storedUser) {
      try {
        const parsedUser = JSON.parse(storedUser);
        setUser(parsedUser);
      } catch (error) {
        console.error('Ошибка парсинга данных пользователя:', error);
      }
    }
    
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      await Promise.all([
        fetchUserInfo(),
        fetchSubscriptionInfo(),
        fetchAvailablePlans()
      ]);
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchUserInfo = async () => {
    if (!token) {
      console.log('Токен не найден, пропускаем загрузку информации о пользователе');
      return;
    }
    
    try {
      const response = await axios.get('/api/auth/user-info', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (response.data.success) {
        setUser(response.data.user);
        // Обновляем localStorage актуальными данными
        localStorage.setItem('user', JSON.stringify(response.data.user));
      }
    } catch (error) {
      console.error('Error fetching user info:', error);
    }
  };

  const fetchSubscriptionInfo = async () => {
    if (!token) {
      console.log('Токен не найден, пропускаем загрузку информации о подписке');
      return;
    }
    
    try {
      const response = await axios.get('/api/subscription/info', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (response.data.success && response.data.subscription) {
        setSubscription(response.data.subscription);
      }
    } catch (error) {
      console.error('Error fetching subscription info:', error);
    }
  };

  const fetchAvailablePlans = async () => {
    try {
      const response = await axios.get('/api/subscription/plans');
      console.log('Plans API response:', response.data);
      
      if (response.data.success && response.data.plans) {
        // Преобразуем планы от бекенда в нужный формат
        const formattedPlans = response.data.plans.map(plan => ({
          id: plan.planType,
          name: plan.displayName,
          duration: `${plan.days} ${plan.days === 1 ? 'день' : plan.days < 5 ? 'дня' : 'дней'}`,
          price: plan.price,
          originalPrice: null, // Можно добавить логику для originalPrice
          discount: null,
          features: plan.features || getDefaultFeatures(plan.planType),
          popular: plan.planType === 'PLAN_30_DAYS', // Делаем популярным план на 30 дней
          color: getPlanColor(plan.planType),
          gradient: getPlanGradient(plan.planType),
          isFree: plan.price === 0
        }));

        // Сортируем планы: бесплатный первый, потом по цене
        formattedPlans.sort((a, b) => {
          if (a.isFree && !b.isFree) return -1;
          if (!a.isFree && b.isFree) return 1;
          return a.price - b.price;
        });

        setAvailablePlans(formattedPlans);
      }
    } catch (error) {
      console.error('Error fetching available plans:', error);
      // Fallback планы на случай ошибки API
      setAvailablePlans(getFallbackPlans());
    }
  };

  const getDefaultFeatures = (planType) => {
    switch (planType) {
      case 'PLAN_FREE':
        return [
          'Базовая аналитика',
          'Тестовый доступ',
          '7 дней бесплатно',
          'Ознакомление с функционалом'
        ];
      case 'PLAN_30_DAYS':
        return [
          'Финансовая таблица',
          'ABC-анализ',
          'Планирование поставок',
          'Email поддержка'
        ];
      case 'PLAN_60_DAYS':
        return [
          'Полная аналитика продаж',
          'Юнит-экономика',
          'Рекламные кампании',
          'Приоритетная поддержка',
          'Экспорт отчетов'
        ];
      case 'PLAN_90_DAYS':
        return [
          'Премиум аналитика',
          'Неограниченные отчеты',
          'Персональный менеджер',
          'API доступ',
          'Индивидуальные отчеты'
        ];
      default:
        return ['Базовый функционал'];
    }
  };

  const getPlanColor = (planType) => {
    switch (planType) {
      case 'PLAN_FREE':
        return 'var(--color-success)';
      case 'PLAN_30_DAYS':
        return 'var(--color-secondary-green)';
      case 'PLAN_60_DAYS':
        return 'var(--color-primary-purple)';
      case 'PLAN_90_DAYS':
        return 'var(--color-primary-pink)';
      default:
        return 'var(--color-primary-green)';
    }
  };

  const getPlanGradient = (planType) => {
    switch (planType) {
      case 'PLAN_FREE':
        return 'linear-gradient(135deg, #48DD00, #52A529)';
      case 'PLAN_30_DAYS':
        return 'var(--gradient-secondary)';
      case 'PLAN_60_DAYS':
        return 'var(--gradient-purple)';
      case 'PLAN_90_DAYS':
        return 'var(--gradient-pink)';
      default:
        return 'var(--gradient-primary)';
    }
  };

  const getFallbackPlans = () => [
    {
      id: 'PLAN_FREE',
      name: 'Бесплатный тестовый',
      duration: '7 дней',
      price: 0,
      originalPrice: null,
      discount: null,
      features: getDefaultFeatures('PLAN_FREE'),
      popular: false,
      color: getPlanColor('PLAN_FREE'),
      gradient: getPlanGradient('PLAN_FREE'),
      isFree: true
    },
    {
      id: 'PLAN_30_DAYS',
      name: '30 дней',
      duration: '30 дней',
      price: 1499,
      originalPrice: null,
      discount: null,
      features: getDefaultFeatures('PLAN_30_DAYS'),
      popular: true,
      color: getPlanColor('PLAN_30_DAYS'),
      gradient: getPlanGradient('PLAN_30_DAYS'),
      isFree: false
    }
  ];

  const handleSubscribe = async (planId) => {
    try {
      setProcessing(true);
      setSelectedPlan(planId);
      
      const plan = availablePlans.find(p => p.id === planId);
      
      let response;
      if (plan?.isFree) {
        // Для бесплатного плана используем специальный эндпоинт
        let userEmail = user?.email;
        
        // Если email не найден в состоянии, пробуем получить из localStorage
        if (!userEmail && storedUser) {
          try {
            const parsedUser = JSON.parse(storedUser);
            userEmail = parsedUser.email;
          } catch (error) {
            console.error('Ошибка парсинга данных пользователя:', error);
          }
        }
        
        if (!userEmail) {
          showNotification('Для оформления подписки необходимо авторизоваться', 'error');
          return;
        }
        
        // Пробуем сначала приватный эндпоинт, потом публичный
        try {
          response = await axios.post('/api/subscription/create-trial', {
            email: userEmail
          });
        } catch (error) {
          if (error.response?.status === 401) {
            // Если не авторизован, используем публичный эндпоинт
            response = await axios.post('/api/public/subscription/free', {
              email: userEmail
            });
          } else {
            throw error;
          }
        }
      } else {
        // Для платных планов используем стандартный эндпоинт
        response = await axios.post('/api/subscription/create', {
          planType: planId,
          paymentMethod: 'card',
          autoRenew: false
        });
      }
      
      if (response.data.success) {
        showNotification(
          plan?.isFree 
            ? 'Бесплатная подписка успешно активирована!' 
            : 'Подписка успешно оформлена!', 
          'success'
        );
        await fetchData(); // Обновляем все данные
      } else {
        showNotification(response.data.message || 'Ошибка оформления подписки', 'error');
      }
    } catch (error) {
      console.error('Error subscribing:', error);
      const errorMessage = error.response?.data?.message || 'Ошибка оформления подписки';
      showNotification(errorMessage, 'error');
    } finally {
      setProcessing(false);
      setSelectedPlan(null);
    }
  };

  const handleCancelSubscription = async () => {
    if (!window.confirm('Вы уверены, что хотите отменить подписку?')) {
      return;
    }
    
    try {
      setProcessing(true);
      
      const response = await axios.post('/api/subscription/cancel', {}, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.data.success) {
        showNotification('Подписка отменена', 'success');
        await fetchData();
      } else {
        showNotification(response.data.message || 'Ошибка отмены подписки', 'error');
      }
    } catch (error) {
      console.error('Error canceling subscription:', error);
      showNotification('Ошибка отмены подписки', 'error');
    } finally {
      setProcessing(false);
    }
  };

  const showNotification = (message, type = 'info') => {
    const notification = document.createElement('div');
    notification.textContent = message;
    notification.className = `notification notification-${type}`;
    notification.style.cssText = `
      position: fixed;
      top: 100px;
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
      if (document.body.contains(notification)) {
        document.body.removeChild(notification);
      }
    }, 4000);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const getDaysRemaining = (endDate) => {
    const now = new Date();
    const end = new Date(endDate);
    const diffTime = end - now;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return Math.max(0, diffDays);
  };

  const getPlanDisplayName = (planType) => {
    const plan = availablePlans.find(p => p.id === planType);
    return plan?.name || planType;
  };

  if (loading) {
    return (
      <div className="subscription-page">
        <div className="container">
          <div className="loading-screen">
            <div className="loading-spinner"></div>
            <p>Загрузка информации о подписках...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="subscription-page">
      <div className="container">
        {/* Заголовок */}
        <div className="subscription-header">
          <div className="header-content">
            <h1 className="page-title">
              <span className="title-icon">💎</span>
              Подписки SellLab
            </h1>
            <p className="page-subtitle">
              Выберите план, который подходит именно вам. Начните с бесплатного тестирования!
            </p>
          </div>
          
          {subscription && subscription.status === 'ACTIVE' && (
            <div className="current-subscription">
              <div className="subscription-badge">
                <span className="badge-icon">✅</span>
                Активная подписка
              </div>
              <div className="subscription-details">
                <div className="subscription-plan">
                  {getPlanDisplayName(subscription.planType)}
                </div>
                <div className="subscription-expires">
                  До {formatDate(subscription.endDate)} ({getDaysRemaining(subscription.endDate)} дней)
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Тарифные планы */}
        <div className="plans-section">
          <div className="plans-grid">
            {availablePlans.map((plan, index) => (
              <div 
                key={plan.id}
                className={`plan-card ${plan.popular ? 'plan-card-popular' : ''} ${
                  plan.isFree ? 'plan-card-free' : ''
                } ${
                  subscription?.planType === plan.id && subscription?.status === 'ACTIVE' ? 'plan-card-current' : ''
                }`}
                style={{ '--plan-color': plan.color }}
              >
                {plan.isFree && (
                  <div className="plan-badge plan-badge-free">
                    <span>🎁 БЕСПЛАТНО</span>
                  </div>
                )}
                
                {plan.popular && !plan.isFree && (
                  <div className="plan-badge">
                    <span>🔥 Популярный</span>
                  </div>
                )}
                
                {subscription?.planType === plan.id && subscription?.status === 'ACTIVE' && (
                  <div className="plan-current-badge">
                    <span>✅ Текущий план</span>
                  </div>
                )}

                <div className="plan-header">
                  <div className="plan-icon" style={{ background: plan.gradient }}>
                    {plan.isFree ? '🎁' : index === 1 ? '🚀' : index === 2 ? '⚡' : '👑'}
                  </div>
                  <h3 className="plan-name">{plan.name}</h3>
                  <div className="plan-duration">{plan.duration}</div>
                </div>

                <div className="plan-pricing">
                  <div className="plan-price">
                    {plan.isFree ? (
                      <span className="price-free">БЕСПЛАТНО</span>
                    ) : (
                      <>
                        <span className="price-currency">₽</span>
                        <span className="price-amount">{plan.price.toLocaleString()}</span>
                      </>
                    )}
                  </div>
                  
                  {plan.originalPrice && plan.originalPrice > plan.price && (
                    <div className="plan-original-price">
                      <span className="original-price">₽{plan.originalPrice.toLocaleString()}</span>
                      <span className="discount-badge">-{plan.discount}%</span>
                    </div>
                  )}
                  
                  {!plan.isFree && (
                    <div className="plan-price-per-day">
                      ≈ ₽{Math.round(plan.price / parseInt(plan.duration))} в день
                    </div>
                  )}
                </div>

                <div className="plan-features">
                  <h4 className="features-title">Что включено:</h4>
                  <ul className="features-list">
                    {plan.features.map((feature, i) => (
                      <li key={i} className="feature-item">
                        <span className="feature-icon">✓</span>
                        <span className="feature-text">{feature}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="plan-action">
                  {subscription?.planType === plan.id && subscription?.status === 'ACTIVE' ? (
                    <button 
                      className="btn btn-current"
                      disabled
                    >
                      <span>✅</span>
                      Активен
                    </button>
                  ) : (
                    <button
                      className={`btn ${plan.isFree ? 'btn-free' : 'btn-primary'}`}
                      onClick={() => handleSubscribe(plan.id)}
                      disabled={processing}
                      style={!plan.isFree ? { background: plan.gradient } : {}}
                    >
                      {processing && selectedPlan === plan.id ? (
                        <>
                          <span className="loading-spinner-small"></span>
                          {plan.isFree ? 'Активация...' : 'Оформление...'}
                        </>
                      ) : (
                        <>
                          <span>{plan.isFree ? '🎁' : '💎'}</span>
                          {plan.isFree ? 'Попробовать бесплатно' : 'Выбрать план'}
                        </>
                      )}
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Информация о подписке */}
        {subscription && subscription.status === 'ACTIVE' && (
          <div className="subscription-info">
            <div className="info-section">
              <h3 className="info-title">Управление подпиской</h3>
              
              <div className="info-grid">
                <div className="info-card">
                  <div className="info-icon">📅</div>
                  <div className="info-content">
                    <h4>Дата начала</h4>
                    <p>{formatDate(subscription.startDate)}</p>
                  </div>
                </div>
                
                <div className="info-card">
                  <div className="info-icon">⏰</div>
                  <div className="info-content">
                    <h4>Дата окончания</h4>
                    <p>{formatDate(subscription.endDate)}</p>
                  </div>
                </div>
                
                <div className="info-card">
                  <div className="info-icon">💰</div>
                  <div className="info-content">
                    <h4>Стоимость</h4>
                    <p>{subscription.price === 0 ? 'Бесплатно' : `₽${subscription.price?.toLocaleString() || 'N/A'}`}</p>
                  </div>
                </div>
                
                <div className="info-card">
                  <div className="info-icon">🔄</div>
                  <div className="info-content">
                    <h4>Автопродление</h4>
                    <p>{subscription.autoRenew ? 'Включено' : 'Отключено'}</p>
                  </div>
                </div>
              </div>
              
              <div className="info-actions">
                <button
                  className="btn btn-danger"
                  onClick={handleCancelSubscription}
                  disabled={processing}
                >
                  {processing ? (
                    <>
                      <span className="loading-spinner-small"></span>
                      Отмена...
                    </>
                  ) : (
                    <>
                      <span>❌</span>
                      Отменить подписку
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* FAQ */}
        <div className="faq-section">
          <div className="section-header">
            <h2 className="section-title">Часто задаваемые вопросы</h2>
          </div>
          
          <div className="faq-grid">
            <div className="faq-item">
              <h4 className="faq-question">🎁 Что включает бесплатный план?</h4>
              <p className="faq-answer">
                Бесплатный тестовый план дает полный доступ ко всем функциям на 7 дней. 
                Это отличная возможность познакомиться с платформой перед покупкой.
              </p>
            </div>
            
            <div className="faq-item">
              <h4 className="faq-question">🤔 Можно ли отменить подписку?</h4>
              <p className="faq-answer">
                Да, вы можете отменить подписку в любой момент. Доступ к премиум функциям 
                сохранится до окончания оплаченного периода.
              </p>
            </div>
            
            <div className="faq-item">
              <h4 className="faq-question">💳 Какие способы оплаты доступны?</h4>
              <p className="faq-answer">
                Мы принимаем все основные банковские карты, а также электронные кошельки 
                и банковские переводы.
              </p>
            </div>
            
            <div className="faq-item">
              <h4 className="faq-question">📊 Сохранятся ли мои данные?</h4>
              <p className="faq-answer">
                Все ваши данные и отчеты сохраняются даже после отмены подписки. 
                Вы сможете получить к ним доступ при возобновлении подписки.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SubscriptionPage;