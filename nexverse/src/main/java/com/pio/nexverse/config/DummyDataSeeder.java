package com.pio.nexverse.config;

import com.pio.nexverse.entities.course.Course;
import com.pio.nexverse.entities.course.CourseModule;
import com.pio.nexverse.entities.course.CourseSkill;
import com.pio.nexverse.entities.course.ModuleContent;
import com.pio.nexverse.entities.course.Skill;
import com.pio.nexverse.entities.organization.Department;
import com.pio.nexverse.entities.organization.Organization;
import com.pio.nexverse.entities.user.User;
import com.pio.nexverse.enums.*;
import com.pio.nexverse.repository.*;
import com.pio.nexverse.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Seeds a small, realistic demo dataset - organizations, departments, employees, skills,
 * courses (with modules, content, thumbnails, logos and course-content files) - so the app
 * has something real to look at in local/dev environments.
 * <p>
 * Runs once by default (skipped whenever an organization already exists). Set
 * {@code app.dummy-seed.reset=true} (env var {@code DUMMY_SEED_RESET}) to wipe every
 * organization-scoped record and reseed from scratch on the next boot - it does not touch the
 * super admin account. That flag is a one-shot lever for a deliberate reset, not something to
 * leave on: turn it back off (or unset {@code DUMMY_SEED_RESET}) once the reseed has run, or
 * every future restart will nuke the data again.
 * <p>
 * Course content is a small curated catalog (real course/module/lesson titles per department,
 * not generated filler) so the seeded LMS content reads like something a real org would use.
 * Logos, thumbnails and skill icons are generated locally (java.awt/ImageIO) and course
 * documents are built as minimal real PDFs by hand (no PDF library needed); course videos are
 * rendered with ffmpeg when it's on PATH, falling back to a placeholder file otherwise. All
 * files go through the app's own {@link FileStorageService} (FTP), matching the real upload
 * flows - needs the FTP server (docker-compose.ftp.yml) running.
 * <p>
 * ponytail: video/document/thumbnail bytes are generated once into small pools and re-uploaded
 * per organization instead of one unique file per record - keeps the seed fast without making
 * every course link to a suspiciously identical file. Fallback mp4 is a bare ISO-BMFF box
 * shell (not a decodable video); install ffmpeg if real playback matters.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DummyDataSeeder implements ApplicationRunner {
    private static final String[] DEPARTMENT_NAMES = {"Engineering", "Sales", "Marketing", "Human Resources", "Customer Support"};
    private static final int EMPLOYEES_PER_DEPT = 6; // 1 manager + 5 employees

    private static final String[] COMPANY_NAMES = {"Acme Corp", "Globex Industries", "Initech"};
    private static final String[] FIRST_NAMES = {
            "Aarav", "Vivaan", "Aditya", "Isha", "Ananya", "Diya", "Rohan", "Kabir", "Meera", "Sara",
            "James", "Olivia", "Liam", "Emma", "Noah", "Ava", "William", "Sophia", "Benjamin", "Mia",
            "Lucas", "Amelia", "Henry", "Harper", "Alexander", "Evelyn", "Daniel", "Abigail", "Ethan", "Ella"
    };
    private static final String[] LAST_NAMES = {
            "Sharma", "Verma", "Gupta", "Iyer", "Nair", "Reddy", "Khan", "Kapoor", "Chopra", "Bose",
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
            "Anderson", "Taylor", "Thomas", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson", "White"
    };
    private static final String[] SKILL_NAMES = {
            "Cloud Computing", "DevOps", "Cybersecurity", "Java", "Communication", "Negotiation",
            "Data Analysis", "Public Speaking", "Leadership", "Problem Solving"
    };

    private static final int THUMBNAIL_POOL_SIZE = 6;
    private static final int ICON_POOL_SIZE = 6;
    private static final int VIDEO_POOL_SIZE = 4;
    private static final int DOCUMENT_POOL_SIZE = 4;
    private static final Color[] PALETTE = {
            new Color(0x1976D2), new Color(0x388E3C), new Color(0xF57C00), new Color(0xD32F2F),
            new Color(0x7B1FA2), new Color(0x00796B), new Color(0x5D4037), new Color(0x455A64)
    };

    private static final int VIDEO_DURATION_SECONDS = 600;
    private static final int DOCUMENT_DURATION_SECONDS = 900;
    private static final int TEXT_DURATION_SECONDS = 300;

    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final ModuleContentRepository moduleContentRepository;
    private final SkillRepository skillRepository;
    private final DepartmentCourseAccessRepository departmentCourseAccessRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserCourseModuleRepository userCourseModuleRepository;
    private final UserModuleContentRepository userModuleContentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSetPasswordRepository userSetPasswordRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.dummy-seed.reset:false}")
    private boolean resetEnabled;

    private record ContentTemplate(String title, ContentType type, String textBody) {}
    private record ModuleTemplate(String title, String description, List<ContentTemplate> contents) {}
    private record CourseTemplate(String title, String shortDescription, String description, Level level,
                                   CourseVisibility visibility, CourseAccessType accessType,
                                   List<String> skillNames, List<ModuleTemplate> modules) {}

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (resetEnabled) {
            wipeExistingData();
        } else if (organizationRepository.count() > 0) {
            return;
        }

        String encodedPassword = passwordEncoder.encode("Password@123");
        Map<String, List<CourseTemplate>> catalog = buildCourseCatalog();

        log.info("Seeding dummy data: generating demo images and files...");
        List<byte[]> thumbnailPool = new java.util.ArrayList<>();
        for (int i = 0; i < THUMBNAIL_POOL_SIZE; i++) {
            thumbnailPool.add(generateImage(640, 360, PALETTE[i % PALETTE.length], "COURSE"));
        }
        List<byte[]> iconPool = new java.util.ArrayList<>();
        for (int i = 0; i < ICON_POOL_SIZE; i++) {
            iconPool.add(generateImage(128, 128, PALETTE[i % PALETTE.length], "SKILL"));
        }
        List<byte[]> videoPool = new java.util.ArrayList<>();
        for (int i = 0; i < VIDEO_POOL_SIZE; i++) {
            videoPool.add(generateVideo(PALETTE[i % PALETTE.length]));
        }

        for (int o = 1; o <= COMPANY_NAMES.length; o++) {
            String companyName = COMPANY_NAMES[o - 1];
            String domain = slug(companyName) + ".com";
            Map<String, Integer> localPartCounts = new java.util.HashMap<>();

            Organization org = new Organization();
            org.setName(companyName);
            org.setEmail("contact@" + domain);
            org.setPhone(String.format("90000000%02d", o));
            org.setAddress(o + " Main Street");
            org.setCity("City" + o);
            org.setState("State" + o);
            org.setZipCode(String.format("5000%02d", o));
            org.setCountry("India");
            org.setStatus(OrganizationStatus.ACTIVE);
            org = organizationRepository.save(org);

            org.setLogoUrl(uploadFile(generateImage(400, 400, PALETTE[(o - 1) % PALETTE.length], "LOGO"),
                    "image/png", "png", org.getId(), OrganizationFileType.ORGANIZATION_LOGO));
            org = organizationRepository.save(org);

            String adminFirstName = FIRST_NAMES[(o - 1) % FIRST_NAMES.length];
            String adminLastName = LAST_NAMES[(o - 1) % LAST_NAMES.length];

            User admin = new User();
            admin.setFirstName(adminFirstName);
            admin.setLastName(adminLastName);
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setEmail(emailFor(adminFirstName, adminLastName, domain, localPartCounts));
            admin.setPhoneNumber(String.format("91000000%02d", o));
            admin.setPassword(encodedPassword);
            admin.setEmployeeCode("ADM" + o);
            admin.setJobTitle("Organization Admin");
            admin.setOrganization(org);
            admin = userRepository.save(admin);

            List<String> orgThumbnailPaths = new java.util.ArrayList<>();
            for (byte[] thumbnail : thumbnailPool) {
                orgThumbnailPaths.add(uploadFile(thumbnail, "image/png", "png", org.getId(), OrganizationFileType.COURSE_THUMBNAIL));
            }
            List<String> orgVideoPaths = new java.util.ArrayList<>();
            for (byte[] video : videoPool) {
                orgVideoPaths.add(uploadFile(video, "video/mp4", "mp4", org.getId(), OrganizationFileType.COURSE_CONTENT));
            }
            List<String> orgDocumentPaths = new java.util.ArrayList<>();
            for (int i = 0; i < DOCUMENT_POOL_SIZE; i++) {
                byte[] doc = generatePdf("Course Reference Material " + (i + 1),
                        List.of("This handout supports the associated lesson.",
                                "Keep it handy while completing the module.",
                                "Generated for " + companyName + " demo content."));
                orgDocumentPaths.add(uploadFile(doc, "application/pdf", "pdf", org.getId(), OrganizationFileType.COURSE_CONTENT));
            }

            Map<String, Skill> skillsByName = new java.util.HashMap<>();
            for (int s = 0; s < SKILL_NAMES.length; s++) {
                Skill skill = new Skill();
                skill.setName(SKILL_NAMES[s]);
                skill.setDescription("Proficiency in " + SKILL_NAMES[s]);
                skill.setIconUrl(uploadFile(iconPool.get(s % iconPool.size()), "image/png", "png",
                        org.getId(), OrganizationFileType.SKILL_BADGE_ICON));
                skill.setOrganization(org);
                skill.setCreatedBy(admin);
                skillsByName.put(SKILL_NAMES[s], skillRepository.save(skill));
            }

            int contentOrdinal = 0;
            for (int d = 0; d < DEPARTMENT_NAMES.length; d++) {
                Department dept = new Department();
                dept.setName(DEPARTMENT_NAMES[d]);
                dept.setOrganization(org);
                dept = departmentRepository.save(dept);

                for (int e = 1; e <= EMPLOYEES_PER_DEPT; e++) {
                    String firstName = FIRST_NAMES[(o + d + e) % FIRST_NAMES.length];
                    String lastName = LAST_NAMES[(o * 7 + d * 3 + e) % LAST_NAMES.length];

                    User employee = new User();
                    employee.setFirstName(firstName);
                    employee.setLastName(lastName);
                    employee.setRole(e == 1 ? Role.MANAGER : Role.EMPLOYEE);
                    employee.setStatus(UserStatus.ACTIVE);
                    employee.setEmail(emailFor(firstName, lastName, domain, localPartCounts));
                    employee.setPhoneNumber(String.format("92%03d%03d%02d", o, d + 1, e));
                    employee.setPassword(encodedPassword);
                    employee.setEmployeeCode("O" + o + "D" + (d + 1) + "E" + e);
                    employee.setJobTitle(e == 1 ? dept.getName() + " Manager" : dept.getName() + " Executive");
                    employee.setOrganization(org);
                    employee.setDepartment(dept);
                    userRepository.save(employee);
                }

                for (CourseTemplate template : catalog.get(dept.getName())) {
                    Course course = new Course();
                    course.setTitle(template.title());
                    course.setShortDescription(template.shortDescription());
                    course.setDescription(template.description());
                    course.setLevel(template.level());
                    course.setVisibility(template.visibility());
                    course.setAccessType(template.accessType());
                    course.setStatus(ResourceCreationStatus.PUBLISHED);
                    course.setPublishedAt(LocalDateTime.now());
                    course.setPublishedBy(admin);
                    course.setCreatedBy(admin);
                    course.setUpdatedBy(admin);
                    course.setOwningDepartment(dept);
                    course.setThumbnailUrl(orgThumbnailPaths.get(contentOrdinal % orgThumbnailPaths.size()));

                    int courseDuration = 0;
                    List<CourseModule> modules = new java.util.ArrayList<>();
                    for (int m = 0; m < template.modules().size(); m++) {
                        ModuleTemplate moduleTemplate = template.modules().get(m);
                        CourseModule module = new CourseModule();
                        module.setTitle(moduleTemplate.title());
                        module.setDescription(moduleTemplate.description());
                        module.setSequenceOrder(m + 1);
                        module.setCreatedBy(admin);
                        module.setUpdatedBy(admin);
                        module.setCourse(course);

                        int moduleDuration = 0;
                        List<ModuleContent> contents = new java.util.ArrayList<>();
                        for (int c = 0; c < moduleTemplate.contents().size(); c++) {
                            ContentTemplate contentTemplate = moduleTemplate.contents().get(c);
                            ModuleContent content = new ModuleContent();
                            content.setTitle(contentTemplate.title());
                            content.setContentType(contentTemplate.type());
                            content.setSequenceOrder(c + 1);
                            content.setCourseModule(module);
                            content.setCreatedBy(admin);
                            content.setUpdatedBy(admin);

                            int duration = switch (contentTemplate.type()) {
                                case VIDEO -> {
                                    content.setContentUrl(orgVideoPaths.get(contentOrdinal % orgVideoPaths.size()));
                                    content.setFormat("video/mp4");
                                    yield VIDEO_DURATION_SECONDS;
                                }
                                case DOCUMENT -> {
                                    content.setContentUrl(orgDocumentPaths.get(contentOrdinal % orgDocumentPaths.size()));
                                    content.setFormat("application/pdf");
                                    yield DOCUMENT_DURATION_SECONDS;
                                }
                                case TEXT -> {
                                    content.setTextBody(contentTemplate.textBody());
                                    content.setFormat("text/plain");
                                    yield TEXT_DURATION_SECONDS;
                                }
                            };
                            content.setEstimatedDurationSeconds(duration);
                            moduleDuration += duration;
                            contentOrdinal++;
                            contents.add(content);
                        }
                        module.setModuleContents(contents);
                        module.setEstimatedDurationSeconds(moduleDuration);
                        courseDuration += moduleDuration;
                        modules.add(module);
                    }
                    course.setModules(modules);
                    course.setEstimatedDurationSeconds(courseDuration);

                    List<CourseSkill> courseSkills = new java.util.ArrayList<>();
                    for (String skillName : template.skillNames()) {
                        CourseSkill courseSkill = new CourseSkill();
                        courseSkill.setSkill(skillsByName.get(skillName));
                        courseSkill.setSkillLevel(template.level());
                        courseSkill.setCourse(course);
                        courseSkill.setCreatedBy(admin);
                        courseSkills.add(courseSkill);
                    }
                    course.setCourseSkills(courseSkills);

                    courseRepository.save(course);
                }
            }
            log.info("Seeded organization {}/{}: {}", o, COMPANY_NAMES.length, org.getName());
        }
        log.info("Dummy data seeding complete.");
    }

    /**
     * Deletes every organization-scoped record (in FK-safe order) so the next seed pass starts
     * from a clean slate. The super admin account is untouched - it isn't tied to an organization.
     */
    private void wipeExistingData() {
        log.warn("app.dummy-seed.reset=true: wiping all existing organization-scoped data before reseeding.");
        userModuleContentRepository.deleteAll();
        userCourseModuleRepository.deleteAll();
        userCourseRepository.deleteAll();
        departmentCourseAccessRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userSetPasswordRepository.deleteAll();
        courseRepository.deleteAll(); // cascades course_module -> module_content, and course_skill
        skillRepository.deleteAll();
        userRepository.deleteAll(userRepository.findAll().stream().filter(u -> u.getRole() != Role.SUPER_ADMIN).toList());
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    /**
     * A small, real course catalog keyed by department name - the same handful of courses is
     * seeded per organization rather than generating unique-but-meaningless titles per org.
     */
    private Map<String, List<CourseTemplate>> buildCourseCatalog() {
        Map<String, List<CourseTemplate>> catalog = new java.util.LinkedHashMap<>();

        catalog.put("Engineering", List.of(
                new CourseTemplate("Cloud Computing Fundamentals",
                        "Get comfortable with core cloud concepts and services.",
                        "An introduction to cloud computing for engineers: service models, core building blocks, elasticity, and the basics of keeping a cloud environment secure.",
                        Level.BEGINNER, CourseVisibility.ORGANIZATION, CourseAccessType.OPEN,
                        List.of("Cloud Computing", "DevOps"),
                        List.of(
                                new ModuleTemplate("Introduction to Cloud Models", "IaaS, PaaS and SaaS, and why they matter.",
                                        List.of(
                                                new ContentTemplate("What is Cloud Computing?", ContentType.VIDEO, null),
                                                new ContentTemplate("IaaS vs PaaS vs SaaS Cheat Sheet", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Why Enterprises Move to the Cloud", ContentType.TEXT,
                                                        "Organizations move to the cloud to trade upfront capital spending on hardware for elastic, pay-as-you-go infrastructure. Beyond cost, the cloud gives teams faster access to new services, built-in redundancy, and the ability to scale a workload up or down within minutes instead of weeks. The trade-off is a new set of skills: teams need to understand shared responsibility, network configuration, and cost management in an environment they no longer physically control."))),
                                new ModuleTemplate("Core Cloud Services", "Compute, storage, networking and how they fit together.",
                                        List.of(
                                                new ContentTemplate("Compute, Storage and Networking Basics", ContentType.VIDEO, null),
                                                new ContentTemplate("Choosing the Right Cloud Service", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Understanding Elasticity and Scalability", ContentType.TEXT,
                                                        "Elasticity is the ability of a system to automatically add or remove resources as demand changes, while scalability is the system's capacity to handle growth at all. A well-designed cloud workload separates state from compute so that new instances can be added behind a load balancer without manual intervention, and removed again once traffic drops - so you only pay for the capacity you're actually using."))),
                                new ModuleTemplate("Cloud Security Basics", "Shared responsibility and avoiding common mistakes.",
                                        List.of(
                                                new ContentTemplate("Shared Responsibility Model Explained", ContentType.VIDEO, null),
                                                new ContentTemplate("Cloud Security Checklist", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Common Cloud Misconfigurations to Avoid", ContentType.TEXT,
                                                        "Most cloud security incidents don't come from a sophisticated attack - they come from a misconfiguration: a storage bucket left publicly readable, an overly broad access key, or a security group that allows traffic from anywhere. Treating infrastructure configuration as code, reviewed the same way application code is reviewed, catches the majority of these mistakes before they ever reach production.")))
                        )),
                new CourseTemplate("Secure Coding Practices",
                        "Write code that resists the most common real-world attacks.",
                        "A practical look at the OWASP Top 10, input validation, and authentication pitfalls that show up in real production incidents.",
                        Level.INTERMEDIATE, CourseVisibility.DEPARTMENT, CourseAccessType.REQUEST_REQUIRED,
                        List.of("Java", "Cybersecurity"),
                        List.of(
                                new ModuleTemplate("Understanding the OWASP Top 10", "The most common web application vulnerabilities.",
                                        List.of(
                                                new ContentTemplate("A Walkthrough of the OWASP Top 10", ContentType.VIDEO, null),
                                                new ContentTemplate("OWASP Top 10 Reference Guide", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Why Secure Coding Starts at Design Time", ContentType.TEXT,
                                                        "Bolting security onto a finished feature is far more expensive than designing it in from the start. Threat modeling a feature before writing code - asking who could misuse it and how - surfaces the handful of decisions (input boundaries, permission checks, what to log) that are cheap to get right early and expensive to retrofit later."))),
                                new ModuleTemplate("Input Validation and Injection Attacks", "Where untrusted input becomes a vulnerability.",
                                        List.of(
                                                new ContentTemplate("Preventing SQL Injection in Practice", ContentType.VIDEO, null),
                                                new ContentTemplate("Input Validation Patterns", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Sanitization vs Validation: Knowing the Difference", ContentType.TEXT,
                                                        "Validation rejects input that doesn't match what's expected; sanitization tries to make questionable input safe to use. Validation is almost always the stronger control - parameterized queries and allow-lists close off entire classes of injection attacks, while sanitization routines have a long history of missing an edge case an attacker eventually finds."))),
                                new ModuleTemplate("Authentication and Session Management", "Keeping accounts and sessions safe.",
                                        List.of(
                                                new ContentTemplate("Building Secure Login Flows", ContentType.VIDEO, null),
                                                new ContentTemplate("Session Management Best Practices", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Common Password Storage Mistakes", ContentType.TEXT,
                                                        "Storing passwords in plain text or with a fast, unsalted hash are still two of the most common findings in security audits. A slow, salted hashing algorithm designed for passwords turns a stolen database from a catastrophic breach into an expensive, time-consuming one for an attacker to crack."))))
                )));

        catalog.put("Sales", List.of(
                new CourseTemplate("Consultative Selling Fundamentals",
                        "Sell by understanding the customer's problem first.",
                        "Learn how to run discovery conversations, build a value proposition around real customer needs, and handle objections without getting defensive.",
                        Level.BEGINNER, CourseVisibility.ORGANIZATION, CourseAccessType.OPEN,
                        List.of("Communication", "Negotiation"),
                        List.of(
                                new ModuleTemplate("Understanding Customer Needs", "Asking better questions before pitching.",
                                        List.of(
                                                new ContentTemplate("The Art of Asking Discovery Questions", ContentType.VIDEO, null),
                                                new ContentTemplate("Discovery Call Question Bank", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Listening More Than You Talk", ContentType.TEXT,
                                                        "The best discovery calls are the ones where the rep talks the least. Asking an open question and then staying quiet through the pause that follows almost always surfaces more useful information than filling the silence with another question or, worse, a pitch."))),
                                new ModuleTemplate("Building Value Propositions", "Connecting features to what the customer actually cares about.",
                                        List.of(
                                                new ContentTemplate("Crafting a Compelling Value Proposition", ContentType.VIDEO, null),
                                                new ContentTemplate("Value Proposition Canvas", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Features vs Benefits: Speaking the Customer's Language", ContentType.TEXT,
                                                        "A feature is what a product does; a benefit is what that means for the person buying it. Customers rarely buy a feature list - they buy the outcome it produces, so a pitch that translates every feature into a specific benefit for that customer's situation will always land better than a generic tour of capabilities."))),
                                new ModuleTemplate("Handling Objections", "Treating pushback as useful information.",
                                        List.of(
                                                new ContentTemplate("Turning Objections into Opportunities", ContentType.VIDEO, null),
                                                new ContentTemplate("Objection Handling Playbook", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Staying Calm Under Pressure", ContentType.TEXT,
                                                        "An objection is rarely personal - it's usually a sign the customer hasn't yet connected the offer to a problem they care about, or that a real concern (price, timing, risk) needs to be addressed directly. Acknowledging the objection before responding to it, instead of arguing past it, keeps the conversation collaborative rather than adversarial.")))
                        )),
                new CourseTemplate("Negotiation Skills for Sales Professionals",
                        "Reach agreements that hold up after the deal closes.",
                        "Move beyond price haggling into principled negotiation: preparing a BATNA, expanding the pie before dividing it, and closing without pressure tactics.",
                        Level.INTERMEDIATE, CourseVisibility.ORGANIZATION, CourseAccessType.OPEN,
                        List.of("Negotiation", "Communication"),
                        List.of(
                                new ModuleTemplate("Negotiation Foundations", "Preparing before you sit down at the table.",
                                        List.of(
                                                new ContentTemplate("Principled Negotiation Explained", ContentType.VIDEO, null),
                                                new ContentTemplate("BATNA Worksheet", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Preparing Before You Negotiate", ContentType.TEXT,
                                                        "Knowing your best alternative to a negotiated agreement (BATNA) before a negotiation starts changes how you negotiate - it tells you exactly which terms are worth walking away from. Reps who prepare their BATNA, along with the other side's likely constraints, negotiate from a position of clarity instead of reacting in the moment."))),
                                new ModuleTemplate("Creating Win-Win Outcomes", "Growing the deal before splitting it.",
                                        List.of(
                                                new ContentTemplate("Expanding the Pie Before Dividing It", ContentType.VIDEO, null),
                                                new ContentTemplate("Trade-off Planning Template", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Avoiding Common Negotiation Traps", ContentType.TEXT,
                                                        "Treating every negotiation as a single number to fight over leaves value on the table. Trading across the terms that matter differently to each side - payment timing, contract length, onboarding support - often produces an outcome both sides prefer to a straight discount."))),
                                new ModuleTemplate("Closing the Deal", "Recognizing signals and following through.",
                                        List.of(
                                                new ContentTemplate("Recognizing Buying Signals", ContentType.VIDEO, null),
                                                new ContentTemplate("Closing Techniques Reference", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Following Up Without Being Pushy", ContentType.TEXT,
                                                        "A good close feels like a natural next step, not a tactic. Confirming what was agreed, proposing a concrete next date, and following up on the timeline the customer set - rather than one the rep invented - keeps momentum without making the customer feel rushed.")))
                        ))));

        catalog.put("Marketing", List.of(
                new CourseTemplate("Digital Marketing Essentials",
                        "The channels, content and metrics that make up modern marketing.",
                        "A tour of digital marketing channels, on-page SEO fundamentals, and the metrics worth paying attention to when judging a campaign.",
                        Level.BEGINNER, CourseVisibility.ORGANIZATION, CourseAccessType.OPEN,
                        List.of("Data Analysis", "Communication"),
                        List.of(
                                new ModuleTemplate("Marketing Channels Overview", "SEO, SEM, social and email at a glance.",
                                        List.of(
                                                new ContentTemplate("SEO, SEM, Social and Email at a Glance", ContentType.VIDEO, null),
                                                new ContentTemplate("Channel Selection Worksheet", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Choosing Channels Based on Audience", ContentType.TEXT,
                                                        "There's no universally 'best' channel - there's the channel where a specific audience already spends attention. A campaign aimed at busy professionals researching a purchase decision behaves differently on search than the same campaign run as a social ad interrupting someone's feed, and the channel mix should follow the audience, not the other way around."))),
                                new ModuleTemplate("Content and SEO Basics", "Writing content that both readers and search engines value.",
                                        List.of(
                                                new ContentTemplate("Writing Content That Ranks", ContentType.VIDEO, null),
                                                new ContentTemplate("On-Page SEO Checklist", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Keyword Research Fundamentals", ContentType.TEXT,
                                                        "Keyword research starts with the question a customer is actually typing, not the term a company would like to be known for. Matching content to real search intent - informational, comparison, or ready-to-buy - determines both what to write and what to expect it to do for the funnel."))),
                                new ModuleTemplate("Measuring Campaign Performance", "Reading a dashboard without fooling yourself.",
                                        List.of(
                                                new ContentTemplate("Reading a Marketing Dashboard", ContentType.VIDEO, null),
                                                new ContentTemplate("Core Marketing Metrics Glossary", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Vanity Metrics vs Metrics That Matter", ContentType.TEXT,
                                                        "Impressions and follower counts are easy to report but rarely tell you whether a campaign is working. Metrics tied to an actual business outcome - qualified leads, cost per acquisition, pipeline influenced - are harder to move but are the ones worth optimizing for.")))
                        )),
                new CourseTemplate("Content Strategy and Storytelling",
                        "Build a consistent voice and a story worth reading.",
                        "How to define a brand voice, structure a story that holds attention, and plan content so it doesn't get created and forgotten.",
                        Level.INTERMEDIATE, CourseVisibility.ORGANIZATION, CourseAccessType.OPEN,
                        List.of("Communication", "Public Speaking"),
                        List.of(
                                new ModuleTemplate("Finding Your Brand Voice", "Consistency readers can recognize.",
                                        List.of(
                                                new ContentTemplate("Defining a Consistent Brand Voice", ContentType.VIDEO, null),
                                                new ContentTemplate("Brand Voice Worksheet", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Tone vs Voice: What's the Difference", ContentType.TEXT,
                                                        "Voice is a brand's underlying personality and stays constant; tone shifts with context - the same brand voice can sound more serious in a security incident update and lighter in a product launch post. Confusing the two is why some brands read as inconsistent even when every individual piece is well written."))),
                                new ModuleTemplate("Structuring a Story", "A narrative arc that keeps people reading.",
                                        List.of(
                                                new ContentTemplate("The Narrative Arc for Marketers", ContentType.VIDEO, null),
                                                new ContentTemplate("Story Structure Template", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Hooking an Audience in the First Line", ContentType.TEXT,
                                                        "Most readers decide whether to keep reading within the first sentence or two. Leading with the outcome, the tension, or the surprising detail - rather than background context - earns the attention needed to get to the rest of the story."))),
                                new ModuleTemplate("Content Calendars and Consistency", "Planning content so it doesn't get lost.",
                                        List.of(
                                                new ContentTemplate("Planning a Quarterly Content Calendar", ContentType.VIDEO, null),
                                                new ContentTemplate("Content Calendar Template", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Repurposing Content Across Channels", ContentType.TEXT,
                                                        "A single piece of long-form content can usually be reshaped into several shorter pieces for different channels without starting from scratch each time. Planning for that reuse up front gets far more mileage out of the same research and writing effort.")))
                        ))));

        catalog.put("Human Resources", List.of(
                new CourseTemplate("Effective Performance Management",
                        "Set clear expectations and give feedback that actually helps.",
                        "A practical approach to goal setting, structured feedback, and handling underperformance fairly and consistently.",
                        Level.INTERMEDIATE, CourseVisibility.ORGANIZATION, CourseAccessType.OPEN,
                        List.of("Leadership", "Communication"),
                        List.of(
                                new ModuleTemplate("Setting Clear Expectations", "Goals that leave no room for ambiguity.",
                                        List.of(
                                                new ContentTemplate("Writing SMART Goals with Employees", ContentType.VIDEO, null),
                                                new ContentTemplate("Goal Setting Template", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Why Ambiguity Kills Performance", ContentType.TEXT,
                                                        "When an employee isn't sure what 'good' looks like, they can't aim for it - and a manager who only clarifies expectations after a miss has waited too long. A goal that specifies what's being measured, by when, and what success looks like removes the guesswork on both sides."))),
                                new ModuleTemplate("Giving Feedback that Lands", "Feedback that changes behavior instead of causing defensiveness.",
                                        List.of(
                                                new ContentTemplate("The SBI Feedback Model", ContentType.VIDEO, null),
                                                new ContentTemplate("Feedback Conversation Guide", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Feedback Timing: Why Sooner is Better", ContentType.TEXT,
                                                        "Feedback given days or weeks after the fact forces the recipient to reconstruct the situation instead of reflecting on it. Addressing a specific situation, behavior and impact close to when it happened keeps the conversation concrete and far less likely to feel like an ambush."))),
                                new ModuleTemplate("Handling Underperformance", "Documenting fairly and giving a real path to improve.",
                                        List.of(
                                                new ContentTemplate("Structuring a Performance Improvement Plan", ContentType.VIDEO, null),
                                                new ContentTemplate("PIP Template", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Documenting Performance Issues Fairly", ContentType.TEXT,
                                                        "Performance documentation protects the employee as much as the organization: specific, dated, factual notes make it possible to distinguish a genuine pattern from a bad week, and give the employee a fair, concrete basis to respond to and improve against.")))
                        )),
                new CourseTemplate("Workplace Diversity and Inclusion",
                        "Build teams where everyone can do their best work.",
                        "Understanding unconscious bias, running inclusive meetings, and practicing everyday allyship.",
                        Level.BEGINNER, CourseVisibility.ORGANIZATION, CourseAccessType.OPEN,
                        List.of("Communication", "Leadership"),
                        List.of(
                                new ModuleTemplate("Understanding Unconscious Bias", "How bias shows up without anyone intending it.",
                                        List.of(
                                                new ContentTemplate("How Unconscious Bias Shows Up at Work", ContentType.VIDEO, null),
                                                new ContentTemplate("Bias Self-Assessment Worksheet", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Bias Is Not the Same as Bigotry", ContentType.TEXT,
                                                        "Unconscious bias is a normal feature of how the brain simplifies decisions, not a sign of bad intent - which is exactly why it's easy to miss in ourselves. Naming specific moments where bias tends to creep in, like resume screening or meeting airtime, is more useful than treating awareness as a one-time realization."))),
                                new ModuleTemplate("Building Inclusive Teams", "Practices that make participation possible for everyone.",
                                        List.of(
                                                new ContentTemplate("Inclusive Meeting Practices", ContentType.VIDEO, null),
                                                new ContentTemplate("Inclusive Language Guide", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Psychological Safety on Teams", ContentType.TEXT,
                                                        "A team has psychological safety when people believe they can raise a concern, admit a mistake, or ask a question without being punished for it. Teams with higher psychological safety consistently surface problems earlier, because people aren't spending energy managing how they'll be perceived for speaking up."))),
                                new ModuleTemplate("Allyship in Practice", "What supporting colleagues actually looks like day to day.",
                                        List.of(
                                                new ContentTemplate("What Everyday Allyship Looks Like", ContentType.VIDEO, null),
                                                new ContentTemplate("Allyship Action Checklist", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Speaking Up Without Derailing", ContentType.TEXT,
                                                        "Effective allyship is usually small and specific - crediting someone whose idea got talked over, or asking a follow-up question that brings a quieter voice back into the conversation - rather than a single grand gesture. Consistency matters more than intensity.")))
                        ))));

        catalog.put("Customer Support", List.of(
                new CourseTemplate("Delivering Exceptional Customer Service",
                        "The fundamentals every support conversation relies on.",
                        "Service standards, active listening, and a simple framework for resolving issues efficiently without leaving customers feeling unheard.",
                        Level.BEGINNER, CourseVisibility.ORGANIZATION, CourseAccessType.OPEN,
                        List.of("Communication", "Problem Solving"),
                        List.of(
                                new ModuleTemplate("Customer Service Fundamentals", "What customers actually expect from support.",
                                        List.of(
                                                new ContentTemplate("What Customers Actually Want", ContentType.VIDEO, null),
                                                new ContentTemplate("Service Standards Reference", ContentType.DOCUMENT, null),
                                                new ContentTemplate("First Impressions in Support Conversations", ContentType.TEXT,
                                                        "A customer forms an impression of the whole company within the first few lines of a support conversation. Acknowledging the issue and showing that it's understood, before jumping to a solution, sets a tone of being heard that carries through the rest of the interaction."))),
                                new ModuleTemplate("Empathy and Active Listening", "Hearing the problem behind the complaint.",
                                        List.of(
                                                new ContentTemplate("Listening for the Real Problem", ContentType.VIDEO, null),
                                                new ContentTemplate("Empathy Statement Bank", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Empathy Without Over-Promising", ContentType.TEXT,
                                                        "Empathy means acknowledging how a customer feels, not agreeing to whatever they ask for. Validating frustration honestly, while still being clear about what can and can't be done, keeps trust intact even when the answer isn't the one the customer wanted."))),
                                new ModuleTemplate("Resolving Issues Efficiently", "A framework for getting to resolution.",
                                        List.of(
                                                new ContentTemplate("A Simple Framework for Ticket Resolution", ContentType.VIDEO, null),
                                                new ContentTemplate("Troubleshooting Flowchart", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Knowing When to Escalate", ContentType.TEXT,
                                                        "Escalating too early wastes a specialist's time on something first-line support could resolve; escalating too late leaves a customer stuck longer than necessary. A clear escalation threshold - tied to complexity or account impact rather than how long the ticket has been open - keeps both risks in check.")))
                        )),
                new CourseTemplate("Conflict De-escalation Techniques",
                        "Keep a heated conversation from getting worse.",
                        "Recognizing escalation triggers early, de-escalation techniques for a live conversation, and setting boundaries respectfully.",
                        Level.INTERMEDIATE, CourseVisibility.ORGANIZATION, CourseAccessType.OPEN,
                        List.of("Communication", "Problem Solving"),
                        List.of(
                                new ModuleTemplate("Recognizing Escalation Triggers", "Spotting frustration before it boils over.",
                                        List.of(
                                                new ContentTemplate("Spotting Frustration Before It Boils Over", ContentType.VIDEO, null),
                                                new ContentTemplate("Escalation Warning Signs Checklist", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Why Tone Matters More Than Words", ContentType.TEXT,
                                                        "A technically correct response delivered in a flat or dismissive tone can escalate a conversation faster than an imperfect one delivered warmly. In written support especially, small word choices - 'unfortunately' versus 'here's what I can do' - shape how a customer reads intent."))),
                                new ModuleTemplate("De-escalation Techniques", "Techniques for a conversation that's already heated.",
                                        List.of(
                                                new ContentTemplate("The LEAP Method for Difficult Conversations", ContentType.VIDEO, null),
                                                new ContentTemplate("De-escalation Script Examples", ContentType.DOCUMENT, null),
                                                new ContentTemplate("Staying Calm When a Customer Isn't", ContentType.TEXT,
                                                        "Slowing down your own pace and tone, even when a customer is talking fast and loud, tends to bring the conversation down with it. Matching their intensity almost always makes things worse, however tempting it is in the moment."))),
                                new ModuleTemplate("Setting Boundaries Respectfully", "Saying no without losing the relationship.",
                                        List.of(
                                                new ContentTemplate("Saying No Without Losing the Customer", ContentType.VIDEO, null),
                                                new ContentTemplate("Boundary-Setting Phrases", ContentType.DOCUMENT, null),
                                                new ContentTemplate("When to Involve a Manager", ContentType.TEXT,
                                                        "Bringing in a manager isn't a failure to handle the call - it's the right move once a request falls outside policy, a customer asks for one explicitly, or the conversation has stopped being productive. Recognizing that line early protects both the customer relationship and the rep's own wellbeing.")))
                        ))));

        return catalog;
    }

    private byte[] generateImage(int width, int height, Color background, String label) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(background);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, height / 5));
        FontMetrics metrics = g.getFontMetrics();
        int x = (width - metrics.stringWidth(label)) / 2;
        int y = (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g.drawString(label, Math.max(x, 0), y);
        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    /**
     * Renders a short solid-color clip with ffmpeg (already on PATH in dev) so seeded video
     * content is a real, playable mp4. Falls back to a bare (non-playable) ISO-BMFF shell if
     * ffmpeg isn't available, so seeding still succeeds on a machine without it.
     */
    private byte[] generateVideo(Color color) {
        try {
            Path tmp = Files.createTempFile("seed-video", ".mp4");
            try {
                String hex = String.format("0x%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
                Process process = new ProcessBuilder(
                        "ffmpeg", "-y", "-f", "lavfi", "-i", "color=c=" + hex + ":s=480x270:d=3",
                        "-pix_fmt", "yuv420p", tmp.toString())
                        .redirectErrorStream(true)
                        .start();
                process.getInputStream().readAllBytes();
                boolean finished = process.waitFor(30, TimeUnit.SECONDS);
                if (finished && process.exitValue() == 0) {
                    return Files.readAllBytes(tmp);
                }
                log.warn("ffmpeg exited abnormally while generating seed video; using placeholder file instead.");
            } finally {
                Files.deleteIfExists(tmp);
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("ffmpeg unavailable ({}); using placeholder mp4 for seed video content.", e.getMessage());
        }
        return minimalMp4Placeholder();
    }

    private byte[] minimalMp4Placeholder() {
        // Bare ftyp + mdat boxes: valid ISO-BMFF container magic, not a decodable video.
        byte[] ftyp = {0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm', 0, 0, 2, 0, 'i', 's', 'o', 'm', 'm', 'p', '4', '1'};
        byte[] payload = "nexverse-seed-placeholder".getBytes(StandardCharsets.US_ASCII);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(ftyp);
        int mdatSize = 8 + payload.length;
        out.writeBytes(new byte[]{(byte) (mdatSize >> 24), (byte) (mdatSize >> 16), (byte) (mdatSize >> 8), (byte) mdatSize, 'm', 'd', 'a', 't'});
        out.writeBytes(payload);
        return out.toByteArray();
    }

    /** Hand-builds a minimal but valid single-page PDF (title + body lines) - no PDF library needed. */
    private byte[] generatePdf(String title, List<String> bodyLines) throws IOException {
        StringBuilder stream = new StringBuilder();
        stream.append("BT /F1 20 Tf 72 720 Td (").append(escapePdfText(title)).append(") Tj ET\n");
        int y = 690;
        for (String line : bodyLines) {
            stream.append("BT /F1 12 Tf 72 ").append(y).append(" Td (").append(escapePdfText(line)).append(") Tj ET\n");
            y -= 20;
        }
        byte[] streamBytes = stream.toString().getBytes(StandardCharsets.ISO_8859_1);

        List<String> objects = List.of(
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /Resources << /Font << /F1 4 0 R >> >> /MediaBox [0 0 612 792] /Contents 5 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new java.util.ArrayList<>();
        writeAscii(out, "%PDF-1.4\n");
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            writeAscii(out, (i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n");
        }
        offsets.add(out.size());
        writeAscii(out, "5 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
        out.write(streamBytes);
        writeAscii(out, "\nendstream\nendobj\n");

        int xrefStart = out.size();
        int objectCount = offsets.size() + 1; // +1 for the free object 0
        writeAscii(out, "xref\n0 " + objectCount + "\n0000000000 65535 f \n");
        for (int offset : offsets) {
            writeAscii(out, String.format("%010d 00000 n \n", offset));
        }
        writeAscii(out, "trailer\n<< /Size " + objectCount + " /Root 1 0 R >>\nstartxref\n" + xrefStart + "\n%%EOF");
        return out.toByteArray();
    }

    private void writeAscii(ByteArrayOutputStream out, String text) {
        out.writeBytes(text.getBytes(StandardCharsets.ISO_8859_1));
    }

    private String escapePdfText(String text) {
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String uploadFile(byte[] bytes, String contentType, String extension, Long organizationId, OrganizationFileType fileType) {
        MultipartFile file = new ByteArrayMultipartFile(bytes, "seed-file." + extension, contentType);
        return fileStorageService.upload(file, organizationId, fileType);
    }

    private String slug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "");
    }

    /**
     * Builds a firstname.lastname@domain email, falling back to a trailing number
     * (john.smith, john.smith1, john.smith2, ...) when the same name repeats within
     * an organization - same convention real mailboxes use.
     */
    private String emailFor(String firstName, String lastName, String domain, Map<String, Integer> localPartCounts) {
        String base = (firstName + "." + lastName).toLowerCase();
        int occurrence = localPartCounts.merge(base, 1, Integer::sum);
        String localPart = occurrence == 1 ? base : base + (occurrence - 1);
        return localPart + "@" + domain;
    }

    /**
     * Minimal in-memory {@link MultipartFile} so generated file bytes can go through the
     * same {@link FileStorageService#upload} path real uploads use, without pulling in a
     * mock/test dependency into main code.
     */
    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] content;
        private final String originalFilename;
        private final String contentType;

        ByteArrayMultipartFile(byte[] content, String originalFilename, String contentType) {
            this.content = content;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
        }

        @Override
        @NonNull
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        @NonNull
        public byte[] getBytes() {
            return content;
        }

        @Override
        @NonNull
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(@NonNull java.io.File dest) throws IOException {
            try (var out = new java.io.FileOutputStream(dest)) {
                out.write(content);
            }
        }
    }
}
