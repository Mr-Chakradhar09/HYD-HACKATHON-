const API_BASE = 'http://localhost:8080/api';

const getHeaders = () => {
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');
  let callerRole = 'HR';
  let callerLocation = 'CHENNAI';
  if (userStr) {
    try {
      const u = JSON.parse(userStr);
      callerRole = u.role || 'HR';
      callerLocation = (u.location || 'CHENNAI').toUpperCase();
    } catch (e) {
      console.error(e);
    }
  }

  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    'X-User-Role': callerRole,
    'X-User-Location': callerLocation
  };
};

export const api = {
  // Auth & Login
  login: async (identifier, password) => {
    // auth-service takes 'email' and 'password'
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: identifier, password }),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Login failed');
    
    // Normalize response for frontend context
    const token = data.data.token;
    const employeeId = data.data.employeeId;
    const email = data.data.email;
    const role = data.data.role;
    const location = data.data.location;

    // Check virtual onboarding status using localStorage as persistence
    const onboardKey = `onboarded_${employeeId}`;
    const status = localStorage.getItem(onboardKey) === 'true' 
      ? 'ACTIVE' 
      : (role === 'EMPLOYEE' ? 'PENDING_ONBOARDING' : 'ACTIVE');

    const user = {
      id: employeeId,
      employeeId,
      username: email.split('@')[0],
      name: email.split('@')[0].toUpperCase(),
      email,
      role,
      location,
      status
    };

    return { token, user };
  },

  // Verify email by querying user-service profile
  verifyEmail: async (email) => {
    const res = await fetch(`${API_BASE}/users/email/${encodeURIComponent(email)}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Email verification failed');
    return data.data; // { id, employeeId, firstName, lastName, email, role, location }
  },

  // Self-register by creating credentials in auth-service and logging in
  completeRegistration: async (email, password, userProfile) => {
    // If we have user profile context, use it. Otherwise, fetch it first.
    let profile = userProfile;
    if (!profile) {
      profile = await api.verifyEmail(email);
    }

    const payload = {
      employeeId: profile.employeeId,
      email: email,
      password: password,
      role: profile.role,
      location: (profile.location || 'CHENNAI').toUpperCase()
    };

    // Call public auth credential registration endpoint via Gateway (internal route is permitted)
    const createRes = await fetch(`${API_BASE.replace('/api', '')}/internal/auth/create-credentials`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    const createData = await createRes.json();
    if (!createRes.ok) throw new Error(createData.message || 'Failed to create credentials');

    // Login automatically
    return await api.login(email, password);
  },

  // HR Pre-registers new employee in user-service
  registerEmployee: async (email, name, location) => {
    const firstName = name.split(' ')[0] || name;
    const lastName = name.split(' ').slice(1).join(' ') || 'Employee';
    const employeeId = 'EMP' + Math.floor(100000 + Math.random() * 900000);

    const payload = {
      employeeId,
      firstName,
      lastName,
      email,
      role: 'EMPLOYEE',
      location: location.toUpperCase(),
      department: 'Engineering',
      designation: 'Software Engineer',
      businessUnit: 'Digital BU'
    };

    const res = await fetch(`${API_BASE}/users`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Employee registration failed');
    return data.data;
  },

  completeOnboarding: async (email) => {
    // Mark user onboarded locally using localStorage persistence
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const u = JSON.parse(userStr);
        if (u.email === email) {
          u.status = 'ACTIVE';
          localStorage.setItem('user', JSON.stringify(u));
          localStorage.setItem(`onboarded_${u.employeeId}`, 'true');
        }
      } catch (e) {
        console.error(e);
      }
    }
    return true;
  },

  // Survey APIs (routed to survey-service)
  getActiveSurvey: async () => {
    const res = await fetch(`${API_BASE}/surveys/active`, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch active survey');
    return data.data;
  },

  getOnboardingSurvey: async () => {
    const res = await fetch(`${API_BASE}/surveys/active?location=ONBOARDING`, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch onboarding survey');
    return data.data;
  },

  rolloutSurvey: async () => {
    const res = await fetch(`${API_BASE}/surveys/rollout`, {
      method: 'POST',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to rollout survey');
    return data.data;
  },

  submitSurveyResponse: async (surveyId, ratings, openEndedText) => {
    const answers = Object.entries(ratings).map(([qId, rating]) => ({
      questionId: parseInt(qId.replace(/\D/g, '') || qId),
      rating: rating,
    }));
    const payload = {
      surveyId: parseInt(surveyId.toString().replace(/\D/g, '') || surveyId),
      answers,
      comments: openEndedText,
    };
    const res = await fetch(`${API_BASE}/responses`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(payload),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to submit response');
    return data.data;
  },

  getSubmissionStatus: async (surveyId) => {
    const res = await fetch(`${API_BASE}/responses/status?surveyId=${surveyId}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch submission status');
    return data.data;
  },

  // Questions / Question Bank
  getQuestions: async (source, status) => {
    let url = `${API_BASE}/questions?`;
    if (source) url += `source=${source}&`;
    if (status) url += `status=${status}&`;
    
    const res = await fetch(url, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch questions');
    return data.data.content || data.data;
  },

  // Drafts / Review APIs
  getDraftQuestions: async (status) => {
    const url = status ? `${API_BASE}/draft-questions?status=${status}` : `${API_BASE}/draft-questions`;
    const res = await fetch(url, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch drafts');
    return data.data.content || data.data;
  },

  approveDraft: async (id) => {
    const res = await fetch(`${API_BASE}/draft-questions/${id}/approve`, {
      method: 'PUT',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to approve draft');
    return data.data;
  },

  rejectDraft: async (id) => {
    const res = await fetch(`${API_BASE}/draft-questions/${id}/reject`, {
      method: 'PUT',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to reject draft');
    return data.data;
  },

  // Reports / Analytics APIs (routed to reporting-service)
  getReportDashboardOverview: async (month) => {
    const url = month ? `${API_BASE}/reports/dashboard/overview?month=${month}` : `${API_BASE}/reports/dashboard/overview`;
    const res = await fetch(url, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch dashboard overview');
    return data;
  },

  getReportDashboardLocations: async (month) => {
    const url = month ? `${API_BASE}/reports/dashboard/locations?month=${month}` : `${API_BASE}/reports/dashboard/locations`;
    const res = await fetch(url, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch location reports');
    return data;
  },

  getReportDashboardThemes: async (month) => {
    const url = month ? `${API_BASE}/reports/dashboard/themes?month=${month}` : `${API_BASE}/reports/dashboard/themes`;
    const res = await fetch(url, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch theme reports');
    return data;
  },

  generateLocationReport: async (locationType, locationName, month) => {
    const monthQuery = month ? `?month=${month}` : '';
    const res = await fetch(`${API_BASE}/reports/location/${locationType}/${locationName}${monthQuery}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    if (!res.ok) throw new Error('Failed to generate report');
    return await res.json();
  },

  getUsersByLocation: async (location) => {
    const res = await fetch(`${API_BASE}/users/location/${location.toUpperCase()}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Failed to fetch users by location');
    return data.data;
  },

  exportPdf: async (month) => {
    const res = await fetch(`${API_BASE}/reports/export/pdf?month=${month}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    if (!res.ok) throw new Error('Failed to export PDF');
    return await res.blob();
  },

  exportCsv: async (month) => {
    const res = await fetch(`${API_BASE}/reports/export/csv?month=${month}`, {
      method: 'GET',
      headers: getHeaders(),
    });
    if (!res.ok) throw new Error('Failed to export CSV');
    return await res.blob();
  },
};
