# -*- coding: utf-8 -*-
'''
将实验一 ~ 实验五 五份报告合并为一份完整报告（.docx）
套用《实验报告参考模板00931.doc》结构：
  封面 → 目录 → 实验一~五（每份含 1.项目介绍 2.相关开发技术 3.项目设计 4.关键代码 5.运行结果 6.心得）
运行： python gen_merged_report.py
'''

import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

from _report_builder import finish_merged, new_doc  # noqa: E402
import _reports_part1 as p1  # noqa: E402
import _reports_part2 as p2  # noqa: E402

# 输出到工作区根目录（与已有的五份单份报告放在一起）
PROJECT_ROOT = os.path.dirname(os.path.dirname(HERE))
OUT_FILENAME = os.path.join(PROJECT_ROOT, '个人项目实验报告（实验一至实验五）.docx')

REPORTS = [
    dict(no='一', title='搭建 Web 基础项目（Spring Boot / SSM）',
         intro=p1.INTRO_1, tech=p1.tech_1, design=p1.design_1,
         code=p1.code_1, run=p1.run_1, reflect=p1.reflect_1),
    dict(no='二', title='前后端分离与 RESTful API',
         intro=p1.INTRO_2, tech=p1.tech_2, design=p1.design_2,
         code=p1.code_2, run=p1.run_2, reflect=p1.reflect_2),
    dict(no='三', title='Git 协作开发与持续集成',
         intro=p1.INTRO_3, tech=p1.tech_3, design=p1.design_3,
         code=p1.code_3, run=p1.run_3, reflect=p1.reflect_3),
    dict(no='四', title='登录、权限与 AI 安全',
         intro=p2.INTRO_4, tech=p2.tech_4, design=p2.design_4,
         code=p2.code_4, run=p2.run_4, reflect=p2.reflect_4),
    dict(no='五', title='LLM 应用开发与智能功能原型',
         intro=p2.INTRO_5, tech=p2.tech_5, design=p2.design_5,
         code=p2.code_5, run=p2.run_5, reflect=p2.reflect_5),
]


def main():
    doc = new_doc()
    path = finish_merged(doc, REPORTS, OUT_FILENAME)
    print(f'\n已合并 {len(REPORTS)} 个实验为一份报告：')
    print('  -', os.path.basename(path))


if __name__ == '__main__':
    main()
