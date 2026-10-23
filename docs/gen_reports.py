# -*- coding: utf-8 -*-
'''
生成五份实验报告 .docx
运行： python gen_reports.py
'''

import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)

from _report_builder import finish_recent, new_doc  # noqa: E402
import _reports_part1 as p1  # noqa: E402
import _reports_part2 as p2  # noqa: E402

REPORTS = [
    dict(no='一', title='搭建 Web 基础项目（Spring Boot / SSM）',
         filename='实验一 搭建Web基础项目（Spring Boot-SSM）.docx',
         intro=p1.INTRO_1, tech=p1.tech_1, design=p1.design_1,
         code=p1.code_1, run=p1.run_1, reflect=p1.reflect_1),
    dict(no='二', title='前后端分离与 RESTful API',
         filename='实验二 前后端分离与RESTful API.docx',
         intro=p1.INTRO_2, tech=p1.tech_2, design=p1.design_2,
         code=p1.code_2, run=p1.run_2, reflect=p1.reflect_2),
    dict(no='三', title='Git 协作开发与持续集成',
         filename='实验三 Git协作开发与持续集成.docx',
         intro=p1.INTRO_3, tech=p1.tech_3, design=p1.design_3,
         code=p1.code_3, run=p1.run_3, reflect=p1.reflect_3),
    dict(no='四', title='登录、权限与 AI 安全',
         filename='实验四 登录权限与AI安全.docx',
         intro=p2.INTRO_4, tech=p2.tech_4, design=p2.design_4,
         code=p2.code_4, run=p2.run_4, reflect=p2.reflect_4),
    dict(no='五', title='LLM 应用开发与智能功能原型',
         filename='实验五 LLM应用开发与智能功能原型.docx',
         intro=p2.INTRO_5, tech=p2.tech_5, design=p2.design_5,
         code=p2.code_5, run=p2.run_5, reflect=p2.reflect_5),
]


def main():
    made = []
    for r in REPORTS:
        doc = new_doc()
        path = finish_recent(
            doc, r['no'], r['title'], r['filename'],
            r['intro'], r['tech'], r['design'], r['code'], r['run'], r['reflect'])
        made.append(path)
    print(f'\n共生成 {len(made)} 份实验报告。')
    for m in made:
        print('  -', os.path.basename(m))


if __name__ == '__main__':
    main()
