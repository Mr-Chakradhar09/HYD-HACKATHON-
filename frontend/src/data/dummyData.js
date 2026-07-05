export const DUMMY_USERS = {
  employee1: { id: 'USR-001', username: 'employee1', role: 'EMPLOYEE', location: 'Chennai', email: 'emp1@virtusa.com', name: 'Arun Kumar' },
  employee2: { id: 'USR-002', username: 'employee2', role: 'EMPLOYEE', location: 'Pune', email: 'emp2@virtusa.com', name: 'Priya Sharma' },
  hr1: { id: 'USR-101', username: 'hr1', role: 'HR', location: 'Chennai', email: 'hr1@virtusa.com', name: 'Suhas Bhagwate' },
  globalhr: { id: 'USR-201', username: 'globalhr', role: 'GLOBAL_HR', location: 'Hyderabad', email: 'globalhr@virtusa.com', name: 'Ravi Shankar' },
};

export const BASELINE_SURVEYS = {
  Chennai: {
    id: 'SV-001', title: 'July 2026 Pulse Survey', location: 'Chennai', monthYear: '07-2026', active: true,
    questions: [
      { id: 'Q1', text: 'How satisfied are you with your current team collaboration and communication?', category: 'Collaboration', themeTag: 'Baseline' },
      { id: 'Q2', text: 'How do you rate your current work-life balance at Virtusa?', category: 'Work-Life Balance', themeTag: 'Baseline' },
      { id: 'Q3', text: 'Do you see clear career progression and learning opportunities in your current role?', category: 'Growth', themeTag: 'Baseline' },
      { id: 'Q4', text: 'How effectively does your manager communicate project goals and expectations?', category: 'Leadership', themeTag: 'Baseline' },
      { id: 'Q5', text: 'How satisfied are you with the compensation and benefits offered?', category: 'Compensation', themeTag: 'Baseline' },
      { id: 'Q6', text: 'How would you rate the onboarding support you received when joining your current project?', category: 'Onboarding', themeTag: 'Baseline' },
    ],
  },
  Pune: {
    id: 'SV-002', title: 'July 2026 Pulse Survey', location: 'Pune', monthYear: '07-2026', active: true,
    questions: [
      { id: 'Q1', text: 'How well does your team collaborate on cross-functional deliverables?', category: 'Collaboration', themeTag: 'Baseline' },
      { id: 'Q2', text: 'Do you feel you maintain a healthy work-life balance during delivery sprints?', category: 'Work-Life Balance', themeTag: 'Baseline' },
      { id: 'Q3', text: 'Are you satisfied with the technical learning opportunities available to you?', category: 'Growth', themeTag: 'Baseline' },
      { id: 'Q4', text: 'Does your leadership team provide timely feedback on your performance?', category: 'Leadership', themeTag: 'Baseline' },
      { id: 'Q5', text: 'How transparent do you find the appraisal and promotion process?', category: 'Compensation', themeTag: 'Baseline' },
    ],
  },
  Hyderabad: {
    id: 'SV-003', title: 'July 2026 Pulse Survey', location: 'Hyderabad', monthYear: '07-2026', active: true,
    questions: [
      { id: 'Q1', text: 'How satisfied are you with the overall work environment at this location?', category: 'Collaboration', themeTag: 'Baseline' },
      { id: 'Q2', text: 'Do you feel empowered to make decisions within your project scope?', category: 'Leadership', themeTag: 'Baseline' },
      { id: 'Q3', text: 'How would you rate access to mentorship and career guidance?', category: 'Growth', themeTag: 'Baseline' },
      { id: 'Q4', text: 'Are you satisfied with the flexibility in your work schedule?', category: 'Work-Life Balance', themeTag: 'Baseline' },
      { id: 'Q5', text: 'Do you feel recognized and appreciated for your contributions?', category: 'Compensation', themeTag: 'Baseline' },
    ],
  },
};

export const MOCK_RESPONSES = [
  { id: 'RESP-1', surveyId: 'SV-001', username: 'emp01', location: 'Chennai', ratings: { Q1: 8, Q2: 6, Q3: 7, Q4: 9, Q5: 5, Q6: 8 }, openEndedText: 'Team collaboration is great and my manager is very supportive. However, work-life balance has been challenging during the last delivery sprint. I had to work overtime for three consecutive weekends which impacted my personal commitments.' },
  { id: 'RESP-2', surveyId: 'SV-001', username: 'emp02', location: 'Chennai', ratings: { Q1: 9, Q2: 8, Q3: 6, Q4: 8, Q5: 6, Q6: 7 }, openEndedText: 'I appreciate the team dynamics but I feel there are limited growth opportunities for mid-level developers. The learning programs offered are mostly beginner-level and don\'t address advanced cloud architecture or AI-related skills.' },
  { id: 'RESP-3', surveyId: 'SV-001', username: 'emp03', location: 'Chennai', ratings: { Q1: 7, Q2: 5, Q3: 8, Q4: 7, Q5: 4, Q6: 9 }, openEndedText: 'The compensation structure needs revision. Compared to industry benchmarks, our salary bands feel outdated. I would also appreciate more transparency in the annual appraisal process and bonus allocation criteria.' },
  { id: 'RESP-4', surveyId: 'SV-001', username: 'emp04', location: 'Chennai', ratings: { Q1: 8, Q2: 7, Q3: 9, Q4: 8, Q5: 7, Q6: 8 }, openEndedText: 'Overall a positive experience. The onboarding for my current project was excellent. My lead provided clear documentation and pair programming sessions during the first two weeks.' },
  { id: 'RESP-5', surveyId: 'SV-002', username: 'emp05', location: 'Pune', ratings: { Q1: 6, Q2: 5, Q3: 7, Q4: 6, Q5: 5 }, openEndedText: 'Cross-functional collaboration in Pune needs improvement. Different teams operate in silos and there is very limited knowledge sharing between the delivery units. We need more internal tech talks and brown bag sessions.' },
  { id: 'RESP-6', surveyId: 'SV-002', username: 'emp06', location: 'Pune', ratings: { Q1: 7, Q2: 4, Q3: 6, Q4: 5, Q5: 6 }, openEndedText: 'The overtime during quarter-end deadlines is excessive. I often work 12-14 hour days during the last two weeks of each quarter. Managers should plan better to distribute work evenly across the sprint cycles.' },
  { id: 'RESP-7', surveyId: 'SV-002', username: 'emp07', location: 'Pune', ratings: { Q1: 8, Q2: 7, Q3: 8, Q4: 7, Q5: 7 }, openEndedText: 'Great team and supportive leadership. I enjoy the technical challenges in my project. Would love to see more hackathons and innovation time allocated within sprint planning.' },
  { id: 'RESP-8', surveyId: 'SV-002', username: 'emp08', location: 'Pune', ratings: { Q1: 5, Q2: 6, Q3: 5, Q4: 4, Q5: 4 }, openEndedText: 'Leadership communication is lacking. We often hear about project changes from client emails rather than from our own delivery heads. This creates confusion and impacts morale.' },
];

export const MOCK_DRAFTS = [
  { id: 'DRF-1', originalSurveyId: 'SV-001', text: 'What specific changes would help reduce overtime and improve your work-life balance during peak delivery cycles?', category: 'Work-Life Balance', themeTag: 'Stress-Mitigation', approved: false },
  { id: 'DRF-2', originalSurveyId: 'SV-001', text: 'Do you feel you have access to advanced learning programs that align with your career growth aspirations in cloud and AI technologies?', category: 'Growth', themeTag: 'Career-Pathing', approved: false },
  { id: 'DRF-3', originalSurveyId: 'SV-001', text: 'How can the leadership team improve communication regarding project timeline changes and client feedback?', category: 'Leadership', themeTag: 'Communication-Transparency', approved: false },
  { id: 'DRF-4', originalSurveyId: 'SV-002', text: 'How satisfied are you with the clarity and timing of the appraisal cycle and the criteria used for promotions?', category: 'Compensation', themeTag: 'Appraisal-Clarity', approved: false },
  { id: 'DRF-5', originalSurveyId: 'SV-002', text: 'What tools or cross-team initiatives would help break down silos and improve knowledge sharing between delivery units?', category: 'Collaboration', themeTag: 'Knowledge-Sharing', approved: false },
];

export const GLOBAL_STATS = {
  location: 'Global', globalPulseScore: 8.2, npsScore: 55.0, totalSubmissions: 800,
  positiveSentimentPercent: 67.2, neutralSentimentPercent: 20.8, negativeSentimentPercent: 12.0,
};

export const LOCATION_STATS = {
  Chennai: { location: 'Chennai', globalPulseScore: 8.4, npsScore: 62.0, totalSubmissions: 480, positiveSentimentPercent: 72.0, neutralSentimentPercent: 18.0, negativeSentimentPercent: 10.0 },
  Pune: { location: 'Pune', globalPulseScore: 7.9, npsScore: 45.0, totalSubmissions: 320, positiveSentimentPercent: 60.0, neutralSentimentPercent: 25.0, negativeSentimentPercent: 15.0 },
  Hyderabad: { location: 'Hyderabad', globalPulseScore: 8.1, npsScore: 52.0, totalSubmissions: 280, positiveSentimentPercent: 68.0, neutralSentimentPercent: 19.0, negativeSentimentPercent: 13.0 },
};

export const MONTHLY_TREND = [
  { month: 'Feb', Chennai: 7.8, Pune: 7.2, Hyderabad: 7.5, Global: 7.5 },
  { month: 'Mar', Chennai: 8.0, Pune: 7.5, Hyderabad: 7.7, Global: 7.7 },
  { month: 'Apr', Chennai: 8.1, Pune: 7.6, Hyderabad: 7.9, Global: 7.9 },
  { month: 'May', Chennai: 8.3, Pune: 7.7, Hyderabad: 8.0, Global: 8.0 },
  { month: 'Jun', Chennai: 8.2, Pune: 7.8, Hyderabad: 7.8, Global: 7.9 },
  { month: 'Jul', Chennai: 8.4, Pune: 7.9, Hyderabad: 8.1, Global: 8.2 },
];

export const PARTICIPATION_DATA = [
  { month: 'Jan', responses: 620 },
  { month: 'Feb', responses: 680 },
  { month: 'Mar', responses: 710 },
  { month: 'Apr', responses: 750 },
  { month: 'May', responses: 720 },
  { month: 'Jun', responses: 780 },
  { month: 'Jul', responses: 800 },
];

export const MOCK_NOTIFICATIONS = [
  { id: 'N-1', recipient: 'ALL', subject: 'July Pulse Survey is Live!', message: 'The monthly pulse survey for July 2026 is now active. Please complete it before the 15th to share your valuable feedback.', timestamp: '2026-07-01T09:00:00Z' },
  { id: 'N-2', recipient: 'HR', subject: 'AI Draft Questions Ready', message: 'The AI Sentiment Engine has completed analysis of June feedback and generated 5 draft questions for August. Please review and approve.', timestamp: '2026-07-02T14:30:00Z' },
  { id: 'N-3', recipient: 'ALL', subject: 'Pulse Score Improvement - Chennai', message: 'Great news! The Chennai office pulse score improved by 0.2 points this month, driven by positive feedback on team collaboration.', timestamp: '2026-07-03T10:15:00Z' },
  { id: 'N-4', recipient: 'GLOBAL_HR', subject: 'Monthly Analytics Report Available', message: 'The comprehensive sentiment analysis report for June 2026 is ready for download. Key insights: Overall positive trend across all locations.', timestamp: '2026-07-03T16:00:00Z' },
  { id: 'N-5', recipient: 'HR', subject: 'Low Pulse Alert - Pune Compensation', message: 'The compensation satisfaction score in Pune dropped below 6.0. Consider scheduling a town hall or initiating a compensation review discussion.', timestamp: '2026-07-04T08:00:00Z' },
];
