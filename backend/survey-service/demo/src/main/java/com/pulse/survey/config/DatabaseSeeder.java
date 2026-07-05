package com.pulse.survey.config;

import com.pulse.survey.entity.Question;
import com.pulse.survey.entity.QuestionVersion;
import com.pulse.survey.enums.QuestionStatus;
import com.pulse.survey.enums.QuestionSource;
import com.pulse.survey.enums.QuestionType;
import com.pulse.survey.repository.QuestionRepository;
import com.pulse.survey.repository.QuestionVersionRepository;
import com.pulse.survey.repository.UserRepository;
import com.pulse.survey.repository.SurveyRepository;
import com.pulse.survey.repository.SurveyQuestionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final QuestionRepository questionRepository;
    private final QuestionVersionRepository versionRepository;
    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (questionRepository.count() == 0) {
            log.info("Database is empty. Seeding initial Question Bank Version 1 with 120 questions...");
            seedQuestions();
        } else {
            log.info("Database already contains questions. Skipping seeder.");
        }
        
        if (userRepository.count() == 0) {
            log.info("Seeding initial users...");
            seedUsers();
        }
        
        if (surveyRepository.findByLocationAndStatus("ONBOARDING", com.pulse.survey.enums.SurveyStatus.PUBLISHED).isEmpty()) {
            log.info("Seeding onboarding survey...");
            seedOnboardingSurvey();
        }
    }

    private void seedQuestions() {
        List<Question> questions = new ArrayList<>();

        String[] categories = {
                "ENGAGEMENT", "WELLBEING", "ALIGNMENT", "GROWTH",
                "LEADERSHIP", "COLLABORATION", "WORKFLOW", "INCLUSION"
        };

        String[][] templates = {
                // ENGAGEMENT
                {
                        "I am proud to work for this organization.",
                        "I would recommend this company as a great place to work.",
                        "I feel motivated to go the extra mile in my role.",
                        "I see myself working here in two years' time.",
                        "I feel energized by the work I do here.",
                        "Our company's mission inspires me to do my best work.",
                        "I look forward to starting my work day.",
                        "I feel a strong sense of commitment to my team.",
                        "My work gives me a sense of personal accomplishment.",
                        "I feel valued and recognized for my contributions.",
                        "I am enthusiastic about the company's future.",
                        "The work environment here motivates me to do my best.",
                        "I feel connected to the company culture.",
                        "I would speak positively about our services to others.",
                        "I feel excited about the projects I am currently working on."
                },
                // WELLBEING
                {
                        "I am able to maintain a healthy work-life balance.",
                        "My workload is manageable and reasonable.",
                        "Our company genuinely cares about employee mental health.",
                        "I feel physically safe in my working environment.",
                        "I can disconnect from work during my personal time.",
                        "My manager supportively encourages work-life boundaries.",
                        "I feel comfortable discussing mental health concerns here.",
                        "I am not experiencing symptoms of work burnout.",
                        "Our wellness programs and benefits support my health needs.",
                        "I am encouraged to take breaks during the work day.",
                        "I feel supported during periods of high stress.",
                        "My team respects my out-of-office and personal time.",
                        "The company offers adequate support for remote/hybrid work.",
                        "I have the flexibility to manage my personal schedule.",
                        "I feel emotionally resilient at work."
                },
                // ALIGNMENT
                {
                        "I understand how my daily work connects to overall company goals.",
                        "The strategic direction of the company is clear to me.",
                        "I understand what is expected of me in my current role.",
                        "Our company's core values align with my personal values.",
                        "I receive regular updates about company performance and changes.",
                        "The company goals are realistic and achievable.",
                        "I understand how my team's projects fit into other departments.",
                        "Our leadership team communicates change transparently.",
                        "I understand our company's competitive advantage.",
                        "The objectives of my department are clear and defined.",
                        "I know how my success is measured in this role.",
                        "Our team priorities align with our department priorities.",
                        "I believe the company's strategy will lead to success.",
                        "I understand the long-term vision of our leadership.",
                        "I am clear on what our customers expect from us."
                },
                // GROWTH
                {
                        "I see clear opportunities for professional growth here.",
                        "My manager regularly discusses my career aspirations with me.",
                        "I have access to training resources that help me do my job.",
                        "The company supports my professional development goals.",
                        "I feel my career is progressing in the right direction.",
                        "I have opportunities to learn new skills on the job.",
                        "Promotions and advancements are handled fairly here.",
                        "I receive support to attend conferences or courses.",
                        "My job challenges me to grow and learn daily.",
                        "I am gaining valuable experience in my current role.",
                        "There is a clear path for advancement within my team.",
                        "I receive constructive guidance on my career goals.",
                        "The skills I develop here will serve my long-term career.",
                        "My manager provides assignments that stretch my abilities.",
                        "I feel confident about my career future in this company."
                },
                // LEADERSHIP
                {
                        "My manager provides clear direction and expectations.",
                        "I receive regular, helpful feedback on my performance.",
                        "My manager treats all team members with respect.",
                        "I trust the decisions made by our executive leadership.",
                        "My manager communicates team updates effectively.",
                        "Our leaders model the values of the company.",
                        "My manager listens to and acts upon my suggestions.",
                        "I feel comfortable sharing disagreeing viewpoints with my manager.",
                        "Our leaders inspire confidence in our team.",
                        "My manager helps me resolve work-related obstacles.",
                        "Our leadership is empathetic and approachable.",
                        "My manager recognizes my achievements regularly.",
                        "I trust my manager's decisions regarding our team.",
                        "Executive leadership keeps us informed of key decisions.",
                        "My manager demonstrates strong leadership capabilities."
                },
                // COLLABORATION
                {
                        "My team works collaboratively to solve complex issues.",
                        "I feel comfortable asking my teammates for assistance.",
                        "We resolve conflicts constructively within our team.",
                        "Information and knowledge are shared openly in my team.",
                        "Departments collaborate effectively across the organization.",
                        "My team members support each other during busy periods.",
                        "We celebrate team successes and milestones together.",
                        "I feel a strong sense of camaraderie with my coworkers.",
                        "Our team meetings are productive and collaborative.",
                        "I trust my teammates to fulfill their responsibilities.",
                        "We communicate constructively, even when we disagree.",
                        "Collaboration is actively encouraged by team managers.",
                        "I feel respected by all members of my team.",
                        "Our team works together to improve our daily processes.",
                        "I feel like I am part of a high-functioning team."
                },
                // WORKFLOW
                {
                        "I have the tools and technology needed to work efficiently.",
                        "Our daily operational processes are simple and clear.",
                        "I have access to the information required to do my job.",
                        "Our internal systems are reliable and modern.",
                        "I do not waste time on redundant administration work.",
                        "We have clear documentation for our common workflows.",
                        "Decisions are made quickly and without unnecessary delays.",
                        "I am empowered to make decisions within my scope of work.",
                        "Our physical office or remote systems support focused work.",
                        "I can easily find resources to solve workspace problems.",
                        "Cross-functional workflows are seamless and documented.",
                        "We have standard templates that simplify our tasks.",
                        "I feel authorized to improve inefficient workflows.",
                        "Our communication tools support my productivity.",
                        "I have clear ownership over my assigned tasks."
                },
                // INCLUSION
                {
                        "I feel a strong sense of belonging at this company.",
                        "Employees of all backgrounds are treated fairly here.",
                        "The company values diverse perspectives and ideas.",
                        "I can bring my authentic self to work every day.",
                        "The company promotes an inclusive work culture.",
                        "I feel my unique identity is respected by my peers.",
                        "We have equal opportunities for career advancement.",
                        "Our managers build teams with diverse skills and views.",
                        "The company has clear anti-discrimination guidelines.",
                        "I feel comfortable voicing my opinions without fear.",
                        "Our team environment is supportive of everyone.",
                        "I see people of diverse backgrounds in leadership roles.",
                        "The company actively supports diversity and inclusion initiatives.",
                        "We speak respectfully about all groups of people.",
                        "I feel accepted for who I am by my manager."
                }
        };

        // Populate 120 questions (8 categories * 15 questions = 120 questions)
        int order = 1;
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            String[] list = templates[i];
            for (String text : list) {
                Question question = Question.builder()
                        .questionText(text)
                        .category(category)
                        .questionType(QuestionType.RATING) // Standard pulse questions are 1-5 ratings
                        .source(QuestionSource.STATIC)
                        .status(QuestionStatus.ACTIVE)
                        .version(1)
                        .createdBy("SYSTEM_SEED")
                        .build();
                questions.add(question);
            }
        }

        // Add 8 TEXT/comment questions to make it a robust bank (e.g. 1 per category, making it 128 total, or adjust templates to fit 120 exactly)
        // Let's adjust to fit exactly 120. Wait, 8 categories * 15 questions = 120. That is exactly 120! Let's make the last question of each category a TEXT question type, so employees can write comments too!
        // That is brilliant. Out of 15 questions, let's make 13 RATING and 2 TEXT questions, or keep all templates and change a few. Let's make 12 questions of type RATING and 3 of type TEXT per category.
        // Let's modify: the last two questions of each category will be TEXT type, and the others will be RATING.
        for (int i = 0; i < categories.length; i++) {
            for (int j = 0; j < 15; j++) {
                int index = i * 15 + j;
                Question q = questions.get(index);
                if (j >= 13) {
                    q.setQuestionType(QuestionType.TEXT);
                    // Append comment prompt
                    q.setQuestionText(q.getQuestionText() + " (Please share your comments/suggestions)");
                }
            }
        }

        questionRepository.saveAll(questions);
        log.info("Saved 120 questions to the database.");

        // Save Version 1 reference
        QuestionVersion v1 = QuestionVersion.builder()
                .version(1)
                .description("Initial Question Bank containing 120 Static Questions across Engagement, Wellbeing, Alignment, Growth, Leadership, Collaboration, Workflow, and Inclusion.")
                .createdDate(LocalDateTime.now())
                .build();
        versionRepository.save(v1);
        log.info("Created Question Bank Version 1.");
    }

    private void seedUsers() {
        userRepository.save(com.pulse.survey.entity.User.builder()
                .email("globalhr@virtusa.com")
                .username("globalhr")
                .name("Ravi Shankar (Global HR)")
                .role(com.pulse.survey.enums.Role.GLOBAL_HR)
                .location("Hyderabad")
                .password(passwordEncoder.encode("password"))
                .status("ACTIVE")
                .build());

        userRepository.save(com.pulse.survey.entity.User.builder()
                .email("hr1@virtusa.com")
                .username("hr1")
                .name("Suhas Bhagwate (HR)")
                .role(com.pulse.survey.enums.Role.HR)
                .location("Chennai")
                .password(passwordEncoder.encode("password"))
                .status("ACTIVE")
                .build());

        userRepository.save(com.pulse.survey.entity.User.builder()
                .email("emp1@virtusa.com")
                .username("employee1")
                .name("Arun Kumar")
                .role(com.pulse.survey.enums.Role.EMPLOYEE)
                .location("Chennai")
                .password(passwordEncoder.encode("password"))
                .status("ACTIVE")
                .build());
    }

    private void seedOnboardingSurvey() {
        List<Question> onboardingQuestions = new ArrayList<>();
        String[] onboardingTexts = {
                "The recruitment and hiring process was smooth and transparent.",
                "I received all necessary laptop hardware, accounts, and system access on my first day.",
                "The onboarding induction sessions and training were helpful and informative.",
                "I feel welcomed by my team members and project lead.",
                "I understand what is expected of me in my role during the first 90 days."
        };

        for (int i = 0; i < onboardingTexts.length; i++) {
            Question q = Question.builder()
                    .questionText(onboardingTexts[i])
                    .category("ONBOARDING")
                    .questionType(QuestionType.RATING)
                    .source(QuestionSource.STATIC)
                    .status(QuestionStatus.ACTIVE)
                    .version(1)
                    .createdBy("SYSTEM_SEED")
                    .build();
            onboardingQuestions.add(q);
        }
        List<Question> savedQs = questionRepository.saveAll(onboardingQuestions);

        com.pulse.survey.entity.Survey survey = com.pulse.survey.entity.Survey.builder()
                .title("Virtusa New Hire Onboarding Survey")
                .month("01")
                .year(2026)
                .location("ONBOARDING")
                .status(com.pulse.survey.enums.SurveyStatus.PUBLISHED)
                .version(1)
                .publishedDate(LocalDateTime.now())
                .build();
        com.pulse.survey.entity.Survey savedSurvey = surveyRepository.save(survey);

        List<com.pulse.survey.entity.SurveyQuestion> links = new ArrayList<>();
        for (int i = 0; i < savedQs.size(); i++) {
            com.pulse.survey.entity.SurveyQuestion link = com.pulse.survey.entity.SurveyQuestion.builder()
                    .survey(savedSurvey)
                    .question(savedQs.get(i))
                    .displayOrder(i + 1)
                    .build();
            links.add(link);
        }
        surveyQuestionRepository.saveAll(links);
        log.info("Successfully seeded onboarding survey with {} questions.", links.size());
    }
}
