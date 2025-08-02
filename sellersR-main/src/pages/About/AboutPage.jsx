import { useState, useEffect } from 'react';
import './AboutPage.css';

const AboutPage = () => {
  const [activeFeature, setActiveFeature] = useState(0);
  const [isLoaded, setIsLoaded] = useState(false);
  const [visibleSections, setVisibleSections] = useState({});
  const [stats, setStats] = useState({
    users: 0,
    sales: 0,
    profit: 0,
    reviews: 0
  });

  const features = [
    {
      icon: '📊',
      title: 'Умная аналитика',
      description: 'Получайте детальные отчеты о продажах, прибыли и эффективности ваших товаров на Wildberries в реальном времени.',
      details: [
        'Анализ продаж по периодам',
        'Отслеживание трендов и сезонности',
        'Сравнение с конкурентами',
        'Прогнозирование спроса'
      ],
      color: 'var(--color-primary-green)',
      gradient: 'var(--gradient-primary)'
    },
    {
      icon: '🚀',
      title: 'Автоматизация рекламы',
      description: 'Используйте 5 стратегий автобиддера для управления CPM и занимайте лучшие позиции с минимальными затратами.',
      details: [
        'Умное управление ставками',
        'Автоматическая оптимизация бюджета',
        'Анализ эффективности кампаний',
        'A/B тестирование объявлений'
      ],
      color: 'var(--color-primary-purple)',
      gradient: 'var(--gradient-purple)'
    },
    {
      icon: '🎯',
      title: 'Управление ключами',
      description: 'Автоминусация фраз и работа по "белому списку" ключей. Полный автопилот для управления кластерами.',
      details: [
        'Автоматический подбор ключевых слов',
        'Минус-слова и стоп-слова',
        'Кластеризация запросов',
        'Мониторинг позиций'
      ],
      color: 'var(--color-primary-pink)',
      gradient: 'var(--gradient-pink)'
    },
    {
      icon: '⚡',
      title: 'Быстрая интеграция',
      description: 'Мгновенная синхронизация данных с API Wildberries. Всегда актуальная информация о ваших продажах.',
      details: [
        'Синхронизация в реальном времени',
        'Безопасное подключение через API',
        'Автоматическое обновление данных',
        'Поддержка всех категорий товаров'
      ],
      color: 'var(--color-secondary-green)',
      gradient: 'var(--gradient-secondary)'
    }
  ];

  // Команда удалена по требованию

  useEffect(() => {
    // Создаем эффект загрузки страницы
    const loadTimer = setTimeout(() => {
      setIsLoaded(true);
    }, 300);

    // Intersection Observer для анимации секций
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            setVisibleSections(prev => ({
              ...prev,
              [entry.target.dataset.section]: true
            }));
          }
        });
      },
      { threshold: 0.1, rootMargin: '50px' }
    );

    // Анимация счетчиков
    const targetStats = {
      users: 10247,
      sales: 50000000,
      profit: 15000000,
      reviews: 4.9
    };

    const duration = 2000; // 2 секунды
    const steps = 60;
    const interval = duration / steps;

    let currentStep = 0;
    const timer = setInterval(() => {
      currentStep++;
      const progress = currentStep / steps;
      
      setStats({
        users: Math.floor(targetStats.users * progress),
        sales: Math.floor(targetStats.sales * progress),
        profit: Math.floor(targetStats.profit * progress),
        reviews: Math.min(targetStats.reviews * progress, 4.9)
      });

      if (currentStep >= steps) {
        clearInterval(timer);
        setStats(targetStats);
      }
    }, interval);

    // Автоматическая смена активной функции
    const featureTimer = setInterval(() => {
      setActiveFeature((prev) => (prev + 1) % features.length);
    }, 4000);

    // Наблюдаем за секциями после загрузки
    setTimeout(() => {
      const sections = document.querySelectorAll('[data-section]');
      sections.forEach(section => observer.observe(section));
    }, 100);

    return () => {
      clearInterval(timer);
      clearInterval(featureTimer);
      clearTimeout(loadTimer);
      observer.disconnect();
    };
  }, []);

  const formatNumber = (num) => {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toLocaleString();
  };

  return (
    <div className={`about-page ${isLoaded ? 'page-loaded' : 'page-loading'}`}>
      {/* Эффект загрузки */}
      <div className="page-entrance-overlay">
        <div className="entrance-particles">
          {[...Array(20)].map((_, i) => (
            <div key={i} className={`particle particle-${i + 1}`}>
              {['✨', '🌟', '💫', '⭐', '🔥'][i % 5]}
            </div>
          ))}
        </div>
      </div>

      {/* Hero секция */}
      <section className="hero" data-section="hero">
        <div className="hero-background">
          <div className="hero-gradient"></div>
          <div className="floating-elements">
            {[...Array(15)].map((_, i) => (
              <div key={i} className={`floating-element floating-element-${i + 1}`}>
                {['📊', '🚀', '💎', '⚡', '🎯'][i % 5]}
              </div>
            ))}
          </div>
        </div>
        
        <div className="container">
          <div className={`hero-content ${visibleSections.hero ? 'content-visible' : ''}`}>
            <div className="hero-text">
              <h1 className="hero-title">
                О проекте <span className="text-gradient">SellLab</span>
              </h1>
              
              <p className="hero-description">
                Мы создали SellLab с единственной целью — помочь продавцам на Wildberries 
                максимизировать прибыль с помощью умной аналитики и автоматизации. 
                Наша платформа объединяет мощные инструменты анализа данных с простотой использования.
              </p>
              
              <div className="hero-stats">
                <div className="stat-item">
                  <div className="stat-value">{stats.users.toLocaleString()}+</div>
                  <div className="stat-label">Активных пользователей</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value">₽{formatNumber(stats.sales)}</div>
                  <div className="stat-label">Проанализированных продаж</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value">₽{formatNumber(stats.profit)}</div>
                  <div className="stat-label">Дополнительной прибыли</div>
                </div>
                <div className="stat-item">
                  <div className="stat-value">{stats.reviews.toFixed(1)} ⭐</div>
                  <div className="stat-label">Средняя оценка</div>
                </div>
              </div>
            </div>
            
            <div className="hero-visual">
              <div className="dashboard-mockup">
                <div className="mockup-header">
                  <div className="mockup-controls">
                    <div className="control red"></div>
                    <div className="control yellow"></div>
                    <div className="control green"></div>
                  </div>
                  <div className="mockup-title">SellLab Dashboard</div>
                </div>
                <div className="mockup-content">
                  <div className="mockup-chart">
                    <svg viewBox="0 0 300 150" className="chart-svg">
                      <defs>
                        <linearGradient id="chartGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                          <stop offset="0%" stopColor="var(--color-primary-green)" />
                          <stop offset="50%" stopColor="var(--color-primary-purple)" />
                          <stop offset="100%" stopColor="var(--color-primary-pink)" />
                        </linearGradient>
                      </defs>
                      <path
                        d="M20,120 Q75,80 150,60 T280,40"
                        stroke="url(#chartGradient)"
                        strokeWidth="4"
                        fill="none"
                        className="animated-path"
                      />
                      <circle cx="280" cy="40" r="6" fill="var(--color-primary-pink)" className="pulse-dot" />
                    </svg>
                  </div>
                  <div className="mockup-metrics">
                    <div className="metric">
                      <div className="metric-label">Выручка</div>
                      <div className="metric-value">₽{formatNumber(stats.sales / 10)}</div>
                    </div>
                    <div className="metric">
                      <div className="metric-label">Прибыль</div>
                      <div className="metric-value">+{Math.floor(stats.profit / stats.sales * 1000) / 10}%</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Функции */}
      <section className="features" data-section="features">
        <div className="container">
          <div className={`section-header ${visibleSections.features ? 'header-visible' : ''}`}>
            <h2 className="section-title">
              Что делает <span className="text-gradient">SellLab</span> особенным?
            </h2>
            <p className="section-description">
              Мы не просто показываем данные — мы помогаем принимать правильные решения
            </p>
          </div>
          
          <div className="features-showcase">
            <div className="features-list">
              {features.map((feature, index) => (
                <div
                  key={index}
                  className={`feature-tab ${index === activeFeature ? 'feature-tab-active' : ''}`}
                  onClick={() => setActiveFeature(index)}
                  style={{ '--feature-color': feature.color }}
                >
                  <div className="feature-tab-icon">{feature.icon}</div>
                  <div className="feature-tab-content">
                    <h3 className="feature-tab-title">{feature.title}</h3>
                    <p className="feature-tab-description">{feature.description}</p>
                  </div>
                </div>
              ))}
            </div>
            
            <div className="feature-details">
              <div className="feature-visual" style={{ background: features[activeFeature].gradient }}>
                <div className="feature-icon-large">
                  {features[activeFeature].icon}
                </div>
              </div>
              
              <div className="feature-content">
                <h3 className="feature-title">{features[activeFeature].title}</h3>
                <p className="feature-description">{features[activeFeature].description}</p>
                
                <ul className="feature-details-list">
                  {features[activeFeature].details.map((detail, i) => (
                    <li key={i} className="detail-item">
                      <span className="detail-icon">✓</span>
                      <span className="detail-text">{detail}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Секция команды удалена */}

      {/* Миссия */}
      <section className="mission" data-section="mission">
        <div className="container">
          <div className={`mission-content ${visibleSections.mission ? 'mission-visible' : ''}`}>
            <div className="mission-text">
              <h2 className="mission-title">
                Наша <span className="text-gradient-pink">миссия</span>
              </h2>
              <p className="mission-description">
                Мы верим, что каждый продавец заслуживает доступа к профессиональным 
                инструментам аналитики. SellLab демократизирует сложные технологии 
                анализа данных, делая их доступными для бизнеса любого размера.
              </p>
              
              <div className="mission-values">
                <div className="value-item">
                  <div className="value-icon">🎯</div>
                  <div className="value-content">
                    <h4 className="value-title">Точность</h4>
                    <p className="value-text">Предоставляем только проверенные и актуальные данные</p>
                  </div>
                </div>
                
                <div className="value-item">
                  <div className="value-icon">⚡</div>
                  <div className="value-content">
                    <h4 className="value-title">Скорость</h4>
                    <p className="value-text">Мгновенная обработка и анализ больших объемов данных</p>
                  </div>
                </div>
                
                <div className="value-item">
                  <div className="value-icon">🤝</div>
                  <div className="value-content">
                    <h4 className="value-title">Поддержка</h4>
                    <p className="value-text">Всегда готовы помочь в достижении ваших целей</p>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="mission-visual">
              <div className="mission-circle">
                <div className="circle-content">
                  <div className="circle-text">
                    <span className="circle-number">{stats.users.toLocaleString()}+</span>
                    <span className="circle-label">довольных клиентов</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="cta" data-section="cta">
        <div className="container">
          <div className={`cta-content ${visibleSections.cta ? 'cta-visible' : ''}`}>
            <h2 className="cta-title">
              Готовы присоединиться к <span className="text-gradient">SellLab</span>?
            </h2>
            <p className="cta-description">
              Начните использовать профессиональную аналитику уже сегодня
            </p>
            <div className="cta-actions">
              <a href="/subscription" className="btn btn-primary">
                <span>🚀</span>
                Начать бесплатно
              </a>
              <a href="/analytics" className="btn btn-outline">
                <span>📊</span>
                Демо версия
              </a>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
};

export default AboutPage;