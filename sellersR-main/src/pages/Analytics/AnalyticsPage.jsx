import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { LineChart, Line, AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, ScatterChart, Scatter } from 'recharts';
import axios from 'axios';
import './AnalyticsPage.css';

const AnalyticsPage = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [hasSubscription, setHasSubscription] = useState(false);
  const [hasApiKey, setHasApiKey] = useState(false);
  const [activeTab, setActiveTab] = useState('financial');
  const [viewMode, setViewMode] = useState('chart');
  const [apiKeyInput, setApiKeyInput] = useState('');
  const [apiKeySaving, setApiKeySaving] = useState(false);
  const [apiKeySuccess, setApiKeySuccess] = useState('');
  const [apiKeyError, setApiKeyError] = useState('');
  const [analyticsData, setAnalyticsData] = useState({
    financial: null,
    'unit-economics': null,
    advertising: null,
    'abc-analysis': null
  });
  const [lastFetchTime, setLastFetchTime] = useState({
    financial: 0,
    'unit-economics': 0,
    advertising: 0,
    'abc-analysis': 0
  });
  const [isRequestInProgress, setIsRequestInProgress] = useState(false);

  const MIN_FETCH_INTERVAL = 30000; // 30 секунд в миллисекундах
  const COLORS = ['#48DD00', '#9F3ED5', '#E6399B', '#52A529', '#AD66D5'];

  // Проверка подписки и API ключа
  const checkSubscriptionAndLoadData = useCallback(async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('Для доступа к аналитике необходимо авторизоваться');
        setLoading(false);
        return;
      }
      
      // Заголовок авторизации будет добавлен автоматически через axios interceptor
      const config = {};
      
      const subscriptionResponse = await axios.get('/api/subscription/info', config);
      
      if (subscriptionResponse.data.success && subscriptionResponse.data.hasSubscription) {
        setHasSubscription(true);
        
        const userData = JSON.parse(localStorage.getItem('user') || '{}');
        const apiKeyResponse = await axios.get('/api/auth/api-key', {
          ...config,
          params: { email: userData.email }
        });
        
        if (apiKeyResponse.data.success && apiKeyResponse.data.hasApiKey) {
          setHasApiKey(true);
          loadAnalyticsData(activeTab);
        }
      } else {
        setHasSubscription(false);
        loadAnalyticsData(activeTab);
      }
    } catch (err) {
      console.error('Error checking requirements:', err);
      setError('Не удалось проверить наличие подписки и API ключа');
      loadAnalyticsData(activeTab);
    } finally {
      setLoading(false);
    }
  }, [activeTab]);

  // Загрузка данных аналитики
  const loadAnalyticsData = useCallback(async (tab) => {
    console.log('🚀 Starting loadAnalyticsData for tab:', tab);
    
    // Проверяем кеш - если данные свежие, используем их
    const now = Date.now();
    const lastFetch = lastFetchTime[tab] || 0;
    if (now - lastFetch < MIN_FETCH_INTERVAL && analyticsData[tab]) {
      console.log('📦 Using cached data for tab:', tab);
      return;
    }
    
    // Простая проверка - если уже загружаем, не делаем новый запрос
    if (loading) {
      console.log('⚠️ Already loading, skipping');
      return;
    }
    
    setLoading(true);
    setError(''); // Очищаем предыдущие ошибки
    
    try {
      // Проверяем токен еще раз перед запросом
      const token = localStorage.getItem('token');
      if (!token) {
        setError('Токен авторизации отсутствует. Пожалуйста, войдите в систему.');
        setLoading(false);
        return;
      }
      
      // Определяем эндпоинт и метод
      let endpoint = '';
      let method = 'GET';
      let requestConfig = {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      };
      
      console.log('🔐 Manually setting Authorization header:', `Bearer ${token.substring(0, 20)}...`);
      
      switch (tab) {
        case 'financial':
          endpoint = '/api/analytics/financial';
          requestConfig.params = { days: 30 };
          break;
        case 'unit-economics':
          endpoint = '/api/analytics/unit-economics';
          method = 'POST';
          requestConfig.params = { days: 30 };
          break;
        case 'advertising':
          endpoint = '/api/analytics/advertising-campaigns';
          method = 'POST';
          requestConfig.params = { days: 7 };
          break;
        case 'abc-analysis':
          endpoint = '/api/analytics/abc-analysis';
          break;
        default:
          endpoint = `/api/analytics/${tab}`;
      }
      
      console.log('📡 Making request to:', endpoint);
      console.log('🔍 Request config:', requestConfig);
      console.log('🔍 Request method:', method);
      
      // 🔍 ДОПОЛНИТЕЛЬНАЯ ОТЛАДКА
      console.log('🎯 BEFORE axios call - URL:', `${axios.defaults.baseURL}${endpoint}`);
      console.log('🎯 BEFORE axios call - Full config:', JSON.stringify(requestConfig, null, 2));
      
      let response;
      if (method === 'POST') {
        console.log('🎯 Calling axios.POST...');
        response = await axios.post(endpoint, {}, requestConfig);
      } else {
              console.log('🎯 Calling axios.GET...');
      
      // 🚨 КРИТИЧЕСКАЯ ОТЛАДКА - добавляем уникальный header
      const uniqueId = Date.now() + Math.random();
      const testConfig = {
        ...requestConfig,
        headers: {
          ...requestConfig.headers,
          'X-Frontend-Request-ID': uniqueId,
          'X-Debug-Source': 'analytics-page',
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache',
          'Expires': '0'
        },
        // Принудительно обходим кеш
        params: {
          ...requestConfig.params,
          '_t': Date.now(), // timestamp для bypass кеша
          '_bypass': uniqueId // уникальный параметр
        }
      };
      
      console.log('🚨 CRITICAL DEBUG - Request ID:', uniqueId);
      console.log('🚨 Final request config:', JSON.stringify(testConfig, null, 2));
      console.log('🚨 Going to:', `${axios.defaults.baseURL}${endpoint}`);
      
      // 🧪 ТЕСТ: Сначала проверим наш тестовый endpoint
      if (endpoint === '/api/analytics/financial') {
        console.log('🧪 TESTING DEBUG ENDPOINT FIRST...');
        try {
          const debugResponse = await axios.get('/api/analytics/debug-test', {
            headers: {
              'X-Frontend-Request-ID': uniqueId + '_DEBUG',
              'X-Debug-Source': 'test-call'
            }
          });
          console.log('🧪 DEBUG ENDPOINT RESPONSE:', debugResponse.data);
          console.log('🧪 DEBUG ENDPOINT SERVER:', debugResponse.data?.server);
        } catch (err) {
          console.error('🧪 DEBUG ENDPOINT FAILED:', err);
        }
      }
      
      response = await axios.get(endpoint, testConfig);
      }
      
      console.log('🎯 AFTER axios call - Response headers:', response.headers);
      console.log('🎯 AFTER axios call - Response config URL:', response.config.url);
      
      console.log('📦 Response received:', response.status, response.data?.success);
      
      // 🔍 ДЕТАЛЬНЫЙ АНАЛИЗ ОТВЕТА
      console.log('🔍 Response data keys:', Object.keys(response.data || {}));
      console.log('🔍 Response data sample:', JSON.stringify(response.data, null, 2).substring(0, 500) + '...');
      console.log('🔍 Response server/source headers:', {
        server: response.headers.server,
        'x-powered-by': response.headers['x-powered-by'],
        'content-type': response.headers['content-type'],
        'cache-control': response.headers['cache-control']
      });
      
      // 🚨 ПРОВЕРЯЕМ ИСТОЧНИК ДАННЫХ
      console.log('🚨 MOCK DATA DETECTION:');
      console.log('🚨 - Response has server header?', !!response.headers.server);
      console.log('🚨 - Response came from network?', response.request ? 'YES' : 'NO');
      console.log('🚨 - Response config valid?', !!response.config);
      console.log('🚨 - Data looks like mock?', 
        response.data?.success === true && 
        response.data?.data?.summary?.totalSales === 0 &&
        response.data?.data?.weeks?.length > 0
      );
      console.log('🚨 - First week date matches today?', response.data?.data?.weeks?.[0]?.date === new Date().toISOString().split('T')[0]);
      
      // Проверяем есть ли признаки mock данных
      if (!response.headers.server && !response.headers['x-powered-by']) {
        console.warn('⚠️⚠️⚠️ SUSPICIOUS: No server headers - likely MOCK DATA!');
      }
      
      if (response.data?.success) {
        let processedData = response.data.data;
        
        // Обработка данных рекламы
        if (tab === 'advertising' && processedData && Array.isArray(processedData) && !processedData.campaigns) {
          processedData = { campaigns: processedData };
        }
        
        setAnalyticsData(prev => ({
          ...prev,
          [tab]: processedData
        }));
        
        setLastFetchTime(prev => ({
          ...prev,
          [tab]: Date.now()
        }));
        
        console.log('✅ Data loaded successfully for tab:', tab);
      } else {
        console.log('❌ Response not successful:', response.data);
        setError(response.data?.message || 'Ошибка загрузки данных');
      }
    } catch (err) {
      console.error('❌ Request failed:', err);
      
      if (err.response?.status === 400 && err.response?.data?.message?.includes('API ключ')) {
        setError('Для просмотра аналитики необходимо добавить API ключ Wildberries в профиле');
      } else if (err.response?.status === 401) {
        setError('Требуется повторная авторизация');
      } else {
        setError('Ошибка загрузки данных: ' + (err.response?.data?.message || err.message));
      }
    } finally {
      setLoading(false);
    }
  }, [loading, analyticsData, lastFetchTime]);

  // Обработчик сохранения API ключа
  const handleSaveApiKey = async (e) => {
    e.preventDefault();
    setApiKeySaving(true);
    setApiKeyError('');
    setApiKeySuccess('');
    
    try {
      const userData = JSON.parse(localStorage.getItem('user') || '{}');
      const token = localStorage.getItem('token');
      const userEmail = userData.email;
      
      if (!userEmail || !token) {
        setApiKeyError('Для установки API ключа необходимо авторизоваться');
        return;
      }
      
      const response = await axios.post('/api/auth/set-api-key', {
        apiKey: apiKeyInput
      }, {
        params: { email: userEmail }
      });
      
      if (response.data.success) {
        setApiKeySuccess(response.data.message || 'API ключ успешно сохранен');
        setHasApiKey(true);
        setApiKeyInput('');
        
        // Принудительно перезагружаем данные после сохранения API ключа
        console.log('API key saved successfully, reloading data for tab:', activeTab);
        setTimeout(() => {
          loadAnalyticsData(activeTab);
        }, 100);
      } else {
        setApiKeyError(response.data.message || 'Ошибка сохранения API ключа');
      }
    } catch (err) {
      console.error('Error saving API key:', err);
      setApiKeyError(err.response?.data?.message || 'Ошибка сохранения API ключа');
    } finally {
      setApiKeySaving(false);
    }
  };

  // Обработчик смены вкладки
  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  // Функции для рендеринга графиков
  const renderFinancialCharts = () => {
    if (!analyticsData.financial?.weeks) return null;
    
    const data = analyticsData.financial.weeks.map(week => ({
      name: `Неделя ${week.week}`,
      sales: week.salesWb,
      profit: week.netProfit,
      logistics: week.logistics,
      storage: week.storage
    }));

    return (
      <div className="charts-container">
        <div className="chart-description">
          <h2>📊 Финансовый отчет</h2>
          <p>Анализ динамики продаж и прибыли по неделям. Зеленая область показывает общий объем продаж, фиолетовая - чистую прибыль после всех расходов.</p>
        </div>
        <div className="chart-wrapper">
          <h3>Динамика продаж и прибыли</h3>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={data}>
              <defs>
                <linearGradient id="colorSales" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#48DD00" stopOpacity={0.3}/>
                  <stop offset="95%" stopColor="#48DD00" stopOpacity={0}/>
                </linearGradient>
                <linearGradient id="colorProfit" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#9F3ED5" stopOpacity={0.3}/>
                  <stop offset="95%" stopColor="#9F3ED5" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
              <XAxis dataKey="name" stroke="rgba(255,255,255,0.6)" fontSize={12} />
              <YAxis stroke="rgba(255,255,255,0.6)" fontSize={12} />
              <Tooltip contentStyle={{ backgroundColor: '#000037', border: '1px solid rgba(255,255,255,0.2)', borderRadius: '8px' }} />
              <Area type="monotone" dataKey="sales" stroke="#48DD00" strokeWidth={3} fillOpacity={1} fill="url(#colorSales)" name="Продажи (₽)" />
              <Area type="monotone" dataKey="profit" stroke="#9F3ED5" strokeWidth={3} fillOpacity={1} fill="url(#colorProfit)" name="Прибыль (₽)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  };

  const renderUnitEconomicsCharts = () => {
    if (!analyticsData['unit-economics']?.items) return null;
    
    const topProducts = [...analyticsData['unit-economics'].items]
      .sort((a, b) => b.finalMarginality - a.finalMarginality)
      .slice(0, 10);
    
    const marginData = topProducts.map(item => ({
      name: item.vendorCode,
      margin: item.finalMarginality,
      profit: item.grossProfitFinal,
      roi: item.roi
    }));
    
    return (
      <div className="charts-container">
        <div className="chart-description">
          <h2>🧮 Юнит-экономика товаров</h2>
          <p>Анализ прибыльности отдельных товаров. Зеленые столбцы показывают маржинальность (%), фиолетовые - возврат инвестиций (ROI). Помогает определить самые выгодные товары.</p>
        </div>
        <div className="chart-wrapper">
          <h3>Топ-10 товаров по маржинальности</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={marginData}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
              <XAxis dataKey="name" stroke="rgba(255,255,255,0.6)" fontSize={12} />
              <YAxis stroke="rgba(255,255,255,0.6)" fontSize={12} />
              <Tooltip contentStyle={{ backgroundColor: '#000037', border: '1px solid rgba(255,255,255,0.2)', borderRadius: '8px' }} />
              <Bar dataKey="margin" fill="#48DD00" name="Маржинальность (%)" />
              <Bar dataKey="roi" fill="#9F3ED5" name="ROI (%)" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  };

  const renderAdvertisingCharts = () => {
    console.log('Rendering advertising charts, data:', analyticsData.advertising);
    
    if (!analyticsData.advertising?.campaigns || analyticsData.advertising.campaigns.length === 0) {
      return (
        <div className="no-data">
          <div className="no-data-icon">📢</div>
          <h3>Нет данных по рекламе</h3>
          <p>Рекламные кампании не найдены или еще не созданы</p>
        </div>
      );
    }
    
    const data = analyticsData.advertising.campaigns.map(campaign => ({
      name: campaign.campaignName || campaign.vendorCode || `Кампания ${campaign.campaignId}`,
      spend: Number(campaign.totalSpend) || 0,
      revenue: Number(campaign.totalRevenue) || 0,
      roas: Number(campaign.roas) || 0,
      clicks: Number(campaign.clicks) || 0
    }));
    
    return (
      <div className="charts-container">
        <div className="chart-description">
          <h2>📢 Рекламные кампании</h2>
          <p>Анализ эффективности рекламных кампаний. Зеленые столбцы показывают затраты на рекламу, фиолетовые - полученную выручку. ROAS показывает возврат рекламных инвестиций.</p>
        </div>
        <div className="chart-wrapper">
          <h3>Расходы и выручка по кампаниям</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={data}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
              <XAxis dataKey="name" stroke="rgba(255,255,255,0.6)" fontSize={12} />
              <YAxis stroke="rgba(255,255,255,0.6)" fontSize={12} />
              <Tooltip contentStyle={{ backgroundColor: '#000037', border: '1px solid rgba(255,255,255,0.2)', borderRadius: '8px' }} />
              <Bar dataKey="spend" fill="#E6399B" name="Затраты (₽)" />
              <Bar dataKey="revenue" fill="#48DD00" name="Выручка (₽)" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  };

  const renderABCAnalysisCharts = () => {
    if (!analyticsData['abc-analysis']) return null;
    
    const { classA, classB, classC } = analyticsData['abc-analysis'].summary;
    const pieData = [
      { name: 'Класс A (80% выручки)', value: classA.percent, color: '#48DD00' },
      { name: 'Класс B (15% выручки)', value: classB.percent, color: '#9F3ED5' },
      { name: 'Класс C (5% выручки)', value: classC.percent, color: '#E6399B' }
    ];
    
    return (
      <div className="charts-container">
        <div className="chart-description">
          <h2>📋 ABC-анализ товаров</h2>
          <p>Классификация товаров по принципу Парето. Класс A - самые важные товары (80% выручки), класс B - средние (15%), класс C - наименее важные (5%). Помогает сосредоточиться на ключевых товарах.</p>
        </div>
        <div className="chart-wrapper">
          <h3>Распределение выручки по классам</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={pieData}
                cx="50%"
                cy="50%"
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
                label={({ name, value }) => `${name}: ${value.toFixed(1)}%`}
              >
                {pieData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip contentStyle={{ backgroundColor: '#000037', border: '1px solid rgba(255,255,255,0.2)', borderRadius: '8px' }} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  };

  // Функции для рендеринга таблиц
  const renderFinancialTable = () => {
    if (!analyticsData.financial?.weeks) {
      return null;
    }
    
    return (
      <div className="financial-report table-container">
        <h2>📊 Финансовый отчет</h2>
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Неделя</th>
                <th>Дата</th>
                <th>Выкуп ШТ</th>
                <th>Продажи ВБ</th>
                <th>К перечислению за товар</th>
                <th>Логистика</th>
                <th>Хранение</th>
                <th>Приемка</th>
                <th>Штраф</th>
                <th>Удержания/реклама</th>
                <th>К выплате</th>
                <th>Налог</th>
                <th>Прочие расходы</th>
                <th>Себестоимость проданного товара</th>
                <th>Чистая прибыль</th>
                <th>ДРР</th>
              </tr>
            </thead>
            <tbody>
              {analyticsData.financial.weeks.map((week, index) => (
                <tr key={index}>
                  <td>{week.week}</td>
                  <td>{week.date}</td>
                  <td>{week.buyoutQuantity?.toLocaleString() || 0}</td>
                  <td>{week.salesWb?.toLocaleString() || 0} ₽</td>
                  <td>{week.toCalculateForGoods?.toLocaleString() || 0} ₽</td>
                  <td>{week.logistics?.toLocaleString() || 0} ₽</td>
                  <td>{week.storage?.toLocaleString() || 0} ₽</td>
                  <td>{week.acceptance?.toLocaleString() || 0} ₽</td>
                  <td>{week.penalty?.toLocaleString() || 0} ₽</td>
                  <td>{week.retentions?.toLocaleString() || 0} ₽</td>
                  <td>{week.toPay?.toLocaleString() || 0} ₽</td>
                  <td>{week.tax?.toLocaleString() || 0} ₽</td>
                  <td>{week.otherExpenses?.toLocaleString() || 0} ₽</td>
                  <td>{week.costOfGoodsSold?.toLocaleString() || 0} ₽</td>
                  <td className={week.netProfit > 0 ? 'profit-positive' : 'profit-negative'}>{week.netProfit?.toLocaleString() || 0} ₽</td>
                  <td>{week.drr?.toFixed(2) || 0}%</td>
                </tr>
              ))}
            </tbody>
            {analyticsData.financial.totals && (
              <tfoot>
                <tr className="totals-row">
                  <td><strong>Итого:</strong></td>
                  <td>-</td>
                  <td><strong>{analyticsData.financial.totals.totalBuyoutQuantity}</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalSalesWb} ₽</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalToCalculateForGoods} ₽</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalLogistics} ₽</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalStorage} ₽</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalAcceptance} ₽</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalPenalty} ₽</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalRetentions} ₽</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalToPay} ₽</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalTax} ₽</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalOtherExpenses} ₽</strong></td>
                  <td><strong>{analyticsData.financial.totals.totalCostOfGoodsSold} ₽</strong></td>
                  <td className={analyticsData.financial.totals.totalNetProfit > 0 ? 'profit-positive' : 'profit-negative'}>
                    <strong>{analyticsData.financial.totals.totalNetProfit} ₽</strong>
                  </td>
                  <td><strong>{analyticsData.financial.totals.avgDrr}%</strong></td>
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      </div>
    );
  };

  const renderUnitEconomicsTable = () => {
    if (!analyticsData['unit-economics']?.items) return null;
    
    return (
      <div className="unit-economics table-container">
        <h2>🧮 Юнит экономика ВБ</h2>
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Артикул ВБ</th>
                <th>Артикул продавца</th>
                <th>Себестоимость</th>
                <th>Цена до СПП</th>
                <th>% СПП</th>
                <th>Цена после СПП</th>
                <th>Выкуп %</th>
                <th>Комиссия МП %</th>
                <th>Логистика МП</th>
                <th>Хранение МП</th>
                <th>Налог</th>
                <th>Выручка после налога</th>
                <th>Валовая прибыль</th>
                <th>Маржинальность</th>
                <th>ROI</th>
                <th>ROM</th>
              </tr>
            </thead>
            <tbody>
              {analyticsData['unit-economics'].items.map((item, index) => (
                <tr key={index}>
                  <td>{item.nmId}</td>
                  <td>{item.vendorCode}</td>
                  <td>{item.costPrice} ₽</td>
                  <td>{item.priceBeforeSpp} ₽</td>
                  <td>{item.sppPercent}%</td>
                  <td>{item.priceAfterSpp} ₽</td>
                  <td>{item.buyout}%</td>
                  <td>{item.mpCommissionPercent}%</td>
                  <td>{item.logisticsMp} ₽</td>
                  <td>{item.storageMp} ₽</td>
                  <td>{item.tax} ₽</td>
                  <td>{item.revenueAfterTax} ₽</td>
                  <td className={item.grossProfitFinal > 0 ? 'profit-positive' : 'profit-negative'}>{item.grossProfitFinal} ₽</td>
                  <td>{item.finalMarginality.toFixed(1)}%</td>
                  <td>{item.roi.toFixed(1)}%</td>
                  <td>{item.rom.toFixed(1)}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  const renderAdvertisingTable = () => {
    console.log('Rendering advertising table, data:', analyticsData.advertising);
    
    if (!analyticsData.advertising?.campaigns || analyticsData.advertising.campaigns.length === 0) {
      return (
        <div className="no-data">
          <div className="no-data-icon">📢</div>
          <h3>Нет данных по рекламе</h3>
          <p>Рекламные кампании не найдены или еще не созданы</p>
        </div>
      );
    }
    
    return (
      <div className="advertising table-container">
        <h2>📢 Рекламные кампании</h2>
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>ID кампании</th>
                <th>Название кампании</th>
                <th>Тип</th>
                <th>Статус</th>
                <th>Затраты</th>
                <th>Выручка</th>
                <th>Клики</th>
                <th>Показы</th>
                <th>CTR</th>
                <th>CPC</th>
                <th>CR</th>
                <th>ROAS</th>
              </tr>
            </thead>
            <tbody>
              {analyticsData.advertising.campaigns.map((campaign, index) => (
                <tr key={index}>
                  <td>{campaign.campaignId || '-'}</td>
                  <td>{campaign.campaignName || `Кампания ${campaign.campaignId || index + 1}`}</td>
                  <td>{campaign.campaignType || 'Не указан'}</td>
                  <td><span className={`status-${(campaign.status || 'unknown').toLowerCase()}`}>{campaign.status || 'Неизвестно'}</span></td>
                  <td className="currency-value">
                    <span className="formatted-number">{Number(campaign.totalSpend || 0).toLocaleString('ru-RU', {minimumFractionDigits: 2, maximumFractionDigits: 2})}</span> ₽
                  </td>
                  <td className="currency-value">
                    <span className="formatted-number">{Number(campaign.totalRevenue || 0).toLocaleString('ru-RU', {minimumFractionDigits: 2, maximumFractionDigits: 2})}</span> ₽
                  </td>
                  <td className="numeric-value">
                    <span className="formatted-number">{Number(campaign.clicks || 0).toLocaleString('ru-RU')}</span>
                  </td>
                  <td className="numeric-value">
                    <span className="formatted-number">{Number(campaign.impressions || 0).toLocaleString('ru-RU')}</span>
                  </td>
                  <td className="percentage-value">
                    <span className="formatted-number">{Number(campaign.ctr || 0).toFixed(2)}</span>%
                  </td>
                  <td className="currency-value">
                    <span className="formatted-number">{Number(campaign.cpc || 0).toFixed(2)}</span> ₽
                  </td>
                  <td className="percentage-value">
                    <span className="formatted-number">{Number(campaign.cr || 0).toFixed(2)}</span>%
                  </td>
                  <td className={Number(campaign.roas || 0) > 2 ? 'profit-positive' : 'profit-negative'}>
                    <span className="formatted-number">{Number(campaign.roas || 0).toFixed(2)}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  const renderABCAnalysisTable = () => {
    if (!analyticsData['abc-analysis']?.items) return null;
    
    return (
      <div className="abc-analysis table-container">
        <h2>📋 ABC-анализ товаров</h2>
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Поз.</th>
                <th>Артикул</th>
                <th>Номенклатура</th>
                <th>Предмет</th>
                <th>Заказы</th>
                <th>Ср. цена</th>
                <th>Выручка</th>
                <th>% группы</th>
                <th>Класс (группа)</th>
                <th>% общий</th>
                <th>Класс (общий)</th>
              </tr>
            </thead>
            <tbody>
              {analyticsData['abc-analysis'].items.map((item, index) => (
                <tr key={index}>
                  <td>{item.position}</td>
                  <td>{item.nmId}</td>
                  <td>{item.vendorCode}</td>
                  <td>{item.subject}</td>
                  <td>{item.ordersCount}</td>
                  <td>{item.avgPrice.toFixed(2)} ₽</td>
                  <td>{item.revenue.toFixed(2)} ₽</td>
                  <td>{item.revenuePercentInGroup.toFixed(1)}%</td>
                  <td><span className={`class-${item.classInGroup.toLowerCase()}`}>{item.classInGroup}</span></td>
                  <td>{item.revenuePercentTotal.toFixed(1)}%</td>
                  <td><span className={`class-${item.classTotal.toLowerCase()}`}>{item.classTotal}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  useEffect(() => {
    checkSubscriptionAndLoadData();
  }, []);

  useEffect(() => {
    console.log('📋 Analytics useEffect triggered:', {
      activeTab,
      hasSubscription,
      hasApiKey,
      shouldLoad: hasSubscription || hasApiKey
    });
    
    if (hasSubscription || hasApiKey) {
      console.log('📋 Calling loadAnalyticsData for:', activeTab);
      // Небольшая задержка чтобы избежать состояния гонки
      setTimeout(() => {
        loadAnalyticsData(activeTab);
      }, 50);
    } else {
      console.log('📋 Not loading - no subscription or API key');
    }
  }, [activeTab, hasSubscription, hasApiKey]);

  if (loading && !hasSubscription && !hasApiKey) {
    return (
      <div className="analytics-page">
        <div className="container">
          <div className="loading-screen">
            <div className="loading-spinner"></div>
            <p>Загрузка аналитики...</p>
          </div>
        </div>
      </div>
    );
  }

  // Убираем блокировку - показываем страницу с предупреждением
  // if (!hasSubscription) {
  //   return (
  //     <div className="analytics-page">
  //       <div className="container">
  //         <div className="subscription-required">
  //           <div className="requirement-icon">⚠️</div>
  //           <h2 className="requirement-title">Требуется подписка</h2>
  //           <p className="requirement-description">
  //             Для доступа к аналитике необходимо оформить подписку
  //           </p>
  //           <Link to="/subscription" className="btn btn-primary">
  //             <span>💎</span>
  //             Оформить подписку
  //           </Link>
  //         </div>
  //       </div>
  //     </div>
  //   );
  // }

  // Убираем блокировку - показываем страницу с предупреждением
  // if (!hasApiKey) {
  //   return (
  //     <div className="analytics-page">
  //       <div className="container">
  //         <div className="api-key-required">
  //           <div className="requirement-icon">🔑</div>
  //           <h2 className="requirement-title">Требуется API ключ Wildberries</h2>
  //           <p className="requirement-description">
  //             Для доступа к аналитике необходимо указать API ключ вашего кабинета Wildberries.
  //           </p>
  //           
  //           <form onSubmit={handleSaveApiKey} className="api-key-form">
  //             {apiKeyError && (
  //               <div className="error-message">
  //                 <span className="error-icon">❌</span>
  //                 {apiKeyError}
  //               </div>
  //             )}
  //             {apiKeySuccess && (
  //               <div className="success-message">
  //                 <span className="success-icon">✅</span>
  //                 {apiKeySuccess}
  //               </div>
  //             )}
  //             
  //             <div className="form-group">
  //               <label htmlFor="apiKey" className="form-label">API ключ Wildberries</label>
  //               <input
  //                 type="text"
  //                 id="apiKey"
  //                 value={apiKeyInput}
  //                 onChange={(e) => setApiKeyInput(e.target.value)}
  //                 className="form-input"
  //                 placeholder="Введите ваш API ключ Wildberries"
  //                 required
  //               />
  //             </div>
  //             
  //             <button 
  //               type="submit" 
  //               className="btn btn-primary"
  //               disabled={apiKeySaving}
  //             >
  //               {apiKeySaving ? (
  //                 <>
  //                   <span className="loading-spinner-small"></span>
  //                   Сохранение...
  //                 </>
  //               ) : (
  //                 <>
  //                   <span>💾</span>
  //                   Сохранить API ключ
  //                 </>
  //               )}
  //             </button>
  //           </form>
  //           
  //           <div className="api-key-help">
  //             <p>
  //               Для получения API ключа перейдите в{' '}
  //               <a 
  //                 href="https://seller.wildberries.ru/supplier-settings/access-to-api" 
  //                 target="_blank" 
  //                 rel="noopener noreferrer"
  //                 className="help-link"
  //               >
  //                 кабинет продавца Wildberries
  //               </a>
  //               {' '}и создайте новый ключ.
  //             </p>
  //           </div>
  //         </div>
  //       </div>
  //     </div>
  //   );
  // }

  if (error) {
    return (
      <div className="analytics-page">
        <div className="container">
          <div className="error-container">
            <div className="error-icon">⚠️</div>
            <h2>Ошибка загрузки данных</h2>
            <p>{error}</p>
            <button 
              className="btn btn-primary"
              onClick={() => loadAnalyticsData(activeTab)}
            >
              <span>🔄</span>
              Повторить попытку
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="analytics-page">
      <div className="container">
        {/* Предупреждения если нет подписки или API ключа */}
        {!hasSubscription && (
          <div className="warning-banner">
            <div className="warning-content">
              <span className="warning-icon">⚠️</span>
              <span>Для полного доступа к аналитике необходимо </span>
              <Link to="/subscription" className="warning-link">оформить подписку</Link>
            </div>
          </div>
        )}
        
        {hasSubscription && !hasApiKey && (
          <div className="warning-banner">
            <div className="warning-content">
              <span className="warning-icon">🔑</span>
              <span>Для получения данных необходимо добавить API ключ Wildberries в </span>
              <Link to="/profile" className="warning-link">профиле</Link>
            </div>
          </div>
        )}
        
        {/* Заголовок */}
        <div className="analytics-header">
          <div className="header-text">
            <h1 className="page-title">
              <span className="title-icon">📊</span>
              Wilberis Analytics
            </h1>
            <p className="page-subtitle">
              Комплексная аналитика ваших продаж на Wildberries
            </p>
          </div>
          
          <div className="header-controls">
            <select className="analytics-select" value={activeTab} onChange={(e) => handleTabChange(e.target.value)}>
              <option value="financial">📊 Финансы</option>
              <option value="unit-economics">💰 Юнит-экономика</option>
              <option value="advertising">📢 Реклама</option>
              <option value="abc-analysis">📋 ABC-анализ</option>
            </select>
            
            <button 
              className="analytics-btn btn-primary"
              onClick={() => loadAnalyticsData(activeTab)}
              disabled={loading}
            >
              {loading ? '⏳' : '🔄'} Обновить
            </button>
            
            <div className="view-mode-toggle">
              <button
                className={`view-mode-btn ${viewMode === 'chart' ? 'view-mode-btn-active' : ''}`}
                onClick={() => setViewMode('chart')}
              >
                <span className="view-icon">📊</span>
                График
              </button>
              <button
                className={`view-mode-btn ${viewMode === 'table' ? 'view-mode-btn-active' : ''}`}
                onClick={() => setViewMode('table')}
              >
                <span className="view-icon">📋</span>
                Таблица
              </button>
            </div>
          </div>
        </div>

        {/* Табы */}
        <div className="analytics-tabs">
          <button 
            className={`tab-button ${activeTab === 'financial' ? 'tab-button-active' : ''}`}
            onClick={() => handleTabChange('financial')}
          >
            <span className="tab-icon">💰</span>
            Финансовый отчет
          </button>
          <button 
            className={`tab-button ${activeTab === 'unit-economics' ? 'tab-button-active' : ''}`}
            onClick={() => handleTabChange('unit-economics')}
          >
            <span className="tab-icon">🧮</span>
            Юнит экономика
          </button>
          <button 
            className={`tab-button ${activeTab === 'advertising' ? 'tab-button-active' : ''}`}
            onClick={() => handleTabChange('advertising')}
          >
            <span className="tab-icon">📢</span>
            Рекламные кампании
          </button>
          <button 
            className={`tab-button ${activeTab === 'abc-analysis' ? 'tab-button-active' : ''}`}
            onClick={() => handleTabChange('abc-analysis')}
          >
            <span className="tab-icon">📋</span>
            ABC-анализ
          </button>
        </div>
        
        {/* Контент */}
        <div className="analytics-content">
          {viewMode === 'chart' ? (
            <>
              {activeTab === 'financial' && renderFinancialCharts()}
              {activeTab === 'unit-economics' && renderUnitEconomicsCharts()}
              {activeTab === 'advertising' && renderAdvertisingCharts()}
              {activeTab === 'abc-analysis' && renderABCAnalysisCharts()}
            </>
          ) : (
            <>
              {activeTab === 'financial' && analyticsData.financial && (
                <>
                  {renderFinancialTable()}
                  <div className="content-divider"></div>
                </>
              )}
              {activeTab === 'unit-economics' && analyticsData['unit-economics'] && (
                <>
                  {renderUnitEconomicsTable()}
                  <div className="content-divider"></div>
                </>
              )}
              {activeTab === 'advertising' && analyticsData.advertising && (
                <>
                  {renderAdvertisingTable()}
                  <div className="content-divider"></div>
                </>
              )}
              {activeTab === 'abc-analysis' && analyticsData['abc-analysis'] && (
                <>
                  {renderABCAnalysisTable()}
                  <div className="content-divider"></div>
                </>
              )}
            </>
          )}
          
          {loading && (
            <div className="loading-screen">
              <div className="loading-spinner"></div>
              <p>Загрузка данных аналитики...</p>
            </div>
          )}
          
          {error && !analyticsData[activeTab] && (
            <div className="error-banner">
              <div className="warning-content">
                <span className="error-icon">❌</span>
                <span>{error}</span>
              </div>
            </div>
          )}
          
          {!analyticsData[activeTab] && !loading && !error && (
            <div className="no-data">
              <div className="no-data-icon">📊</div>
              <h3>Данные отсутствуют</h3>
              <p>Данные для раздела "{activeTab}" не найдены или загружаются</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AnalyticsPage;