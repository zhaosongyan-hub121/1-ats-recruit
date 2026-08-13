package com.recruit.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.entity.*;
import com.recruit.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CompanyMapper companyMapper;
    private final UserMapper userMapper;
    private final PositionMapper positionMapper;
    private final CandidateMapper candidateMapper;
    private final ApplicationMapper applicationMapper;

    @Override
    public void run(String... args) {
        if (companyMapper.selectCount(null) > 0) {
            log.info("数据初始化已存在，跳过示例数据生成");
            return;
        }
        log.info("开始生成示例数据...");
        initCompanies();
        initHrUsers();
        initCandidateUsers();
        initPositions();
        initCandidates();
        initApplications();
        log.info("示例数据生成完成！");
    }

    private void initCompanies() {
        String[][] companies = {
                {"腾讯", "腾", "#0052d9", "互联网/游戏", "中国领先的互联网增值服务提供商", "深圳", "tencent.com", "10000+"},
                {"阿里巴巴", "阿", "#ff6a00", "互联网/电商", "全球领先的电子商务解决方案及服务提供商", "杭州", "alibaba.com", "10000+"},
                {"字节跳动", "字", "#3370ff", "互联网/内容", "全球化的内容平台公司", "北京", "bytedance.com", "10000+"},
                {"美团", "美", "#ffd100", "互联网/生活服务", "中国领先的生活服务电子商务平台", "北京", "meituan.com", "10000+"},
                {"百度", "百", "#2319dc", "互联网/搜索/AI", "全球最大的中文搜索引擎和最大的中文互联网综合服务提供商", "北京", "baidu.com", "10000+"},
                {"京东", "京", "#e1251b", "互联网/零售", "中国领先的技术驱动型电商零售基础设施提供商", "北京", "jd.com", "10000+"}
        };
        for (String[] c : companies) {
            Company company = new Company();
            company.setName(c[0]);
            company.setLogo(c[1]);
            company.setLogoColor(c[2]);
            company.setIndustry(c[3]);
            company.setDescription(c[4]);
            company.setLocation(c[5]);
            company.setWebsite(c[6]);
            company.setSize(c[7]);
            company.setCreatedAt(LocalDateTime.now());
            company.setUpdatedAt(LocalDateTime.now());
            companyMapper.insert(company);
        }
        log.info("初始化 {} 家公司", companies.length);
    }

    private void initHrUsers() {
        String pwdHash = BCrypt.hashpw("admin123", BCrypt.gensalt());
        Object[][] hrs = {
                {"hr_tencent", "张HR", "z***@tencent.com", "13800000001", "HR", "腾讯", 1L},
                {"hr_ali", "李HR", "l*@alibaba.com", "13800000002", "HR", "阿里巴巴", 2L},
                {"hr_bytedance", "王HR", "w***@bytedance.com", "13800000003", "HR", "字节跳动", 3L},
                {"hr_meituan", "赵HR", "z***@meituan.com", "13800000004", "HR", "美团", 4L},
                {"hr_baidu", "孙HR", "s**@baidu.com", "13800000005", "HR", "百度", 5L},
                {"hr_jd", "周HR", "z**@jd.com", "13800000006", "HR", "京东", 6L}
        };
        for (Object[] h : hrs) {
            User user = new User();
            user.setUsername((String) h[0]);
            user.setPassword(pwdHash);
            user.setRealName((String) h[1]);
            user.setEmail((String) h[2]);
            user.setPhone((String) h[3]);
            user.setRole((String) h[4]);
            user.setCompany((String) h[5]);
            user.setCompanyId((Long) h[6]);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        }
        log.info("初始化 {} 个HR账号", hrs.length);
    }

    private void initCandidateUsers() {
        String pwdHash = BCrypt.hashpw("admin123", BCrypt.gensalt());
        Object[][] candidates = {
                {"c_chen", "陈小明", "c***@example.com", "13900000001"},
                {"c_liu", "刘思琪", "l**@example.com", "13900000002"},
                {"c_yang", "杨浩然", "y**@example.com", "13900000003"},
                {"c_huang", "黄雨桐", "h**@example.com", "13900000004"},
                {"c_wu", "吴天翊", "w**@example.com", "13900000005"}
        };
        for (Object[] c : candidates) {
            User user = new User();
            user.setUsername((String) c[0]);
            user.setPassword(pwdHash);
            user.setRealName((String) c[1]);
            user.setEmail((String) c[2]);
            user.setPhone((String) c[3]);
            user.setRole("CANDIDATE");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        }
        log.info("初始化 {} 个求职者账号", candidates.length);
    }

    private void initPositions() {
        log.info("岗位数据已由 schema.sql 初始化，检查是否需要补充...");
        long count = positionMapper.selectCount(null);
        log.info("当前岗位数量: {}", count);
    }

    private void initCandidates() {
        log.info("候选人档案数据检查...");
    }

    private void initApplications() {
        log.info("投递记录数据检查...");
    }
}