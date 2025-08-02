import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import AuthModal from '../Auth/AuthModal';
import './Header.css';

const Header = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const location = useLocation();

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    
    if (token && userData) {
      setIsAuthenticated(true);
      try {
        setUser(JSON.parse(userData));
      } catch (error) {
        console.error('Error parsing user data:', error);
      }
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setIsAuthenticated(false);
    setUser(null);
    window.location.href = '/';
  };

  const handleAuthSuccess = (data) => {
    // Обновляем состояние пользователя
    setIsAuthenticated(true);
    setUser(data.user);
    setShowAuthModal(false);
    
    // Перезагружаем страницу для применения изменений
    window.location.reload();
  };

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  const navItems = [
    { path: '/', label: 'Главная', icon: '🏠' },
    { path: '/analytics', label: 'Аналитика', icon: '📊' },
    { path: '/subscription', label: 'Подписка', icon: '💎' },
    { path: '/profile', label: 'Профиль', icon: '👤' },
    { path: '/about', label: 'О проекте', icon: 'ℹ️' },
  ];

  return (
    <header className="header">
      <div className="container">
        <div className="header-content">
          {/* Логотип */}
          <Link to="/" className="logo-link">
            <div className="logo">
              <img 
                src="/src/assets/logos/selllab-logo.svg" 
                alt="SellLab" 
                className="logo-image"
              />
            </div>
          </Link>

          {/* Основная навигация */}
          <nav className={`nav ${isMenuOpen ? 'nav-open' : ''}`}>
            {navItems.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                className={`nav-link ${location.pathname === item.path ? 'nav-link-active' : ''}`}
                onClick={() => setIsMenuOpen(false)}
              >
                <span className="nav-icon">{item.icon}</span>
                <span className="nav-text">{item.label}</span>
              </Link>
            ))}
          </nav>

          {/* Правая часть */}
          <div className="header-actions">
            {isAuthenticated ? (
              <div className="user-menu">
                <div className="user-info">
                  <div className="user-avatar">
                    {user?.firstName?.charAt(0) || user?.email?.charAt(0) || 'U'}
                  </div>
                  <div className="user-details">
                    <span className="user-name">
                      {user?.firstName || user?.email}
                    </span>
                    <span className="user-status">
                      {user?.isVerified ? '✅ Верифицирован' : '⏳ Не верифицирован'}
                    </span>
                  </div>
                </div>
                <button 
                  className="logout-btn"
                  onClick={handleLogout}
                  title="Выйти"
                >
                  🚪
                </button>
              </div>
            ) : (
              <div className="auth-buttons">
                <button 
                  className="auth-btn auth-btn-outline"
                  onClick={() => setShowAuthModal(true)}
                >
                  Войти
                </button>
                <button 
                  className="auth-btn auth-btn-primary"
                  onClick={() => setShowAuthModal(true)}
                >
                  Регистрация
                </button>
              </div>
            )}

            {/* Мобильное меню */}
            <button 
              className={`mobile-menu-btn ${isMenuOpen ? 'mobile-menu-btn-open' : ''}`}
              onClick={toggleMenu}
              aria-label="Меню"
            >
              <span className="mobile-menu-line"></span>
              <span className="mobile-menu-line"></span>
              <span className="mobile-menu-line"></span>
            </button>
          </div>
        </div>
      </div>

      {/* Мобильное меню overlay */}
      {isMenuOpen && (
        <div 
          className="mobile-overlay"
          onClick={() => setIsMenuOpen(false)}
        />
      )}

      {/* Модальное окно авторизации */}
      <AuthModal
        isOpen={showAuthModal}
        onClose={() => setShowAuthModal(false)}
        onSuccess={handleAuthSuccess}
      />
    </header>
  );
};

export default Header;