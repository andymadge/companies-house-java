Initial prompt given to Claude:

```
We want to write code for integrating with an external API to retrieve company registered addresses. I want to build a set of prompts which will take me through requirements, architecture, planning, implementation and testing using TDD. I want you to follow the XML prompting guide from Anthropic at https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/use-xml-tags and ensure our prompt creation rubric is followed @prompts/rubrics/STD-001-prompt-creation-rubric.md
This will be part of a larger project but at the moment I'm just building the integration with the external API. The general documentation is at https://developer.company-information.service.gov.uk/overview 
I need to retrieve the company registered address so I think I'll need to use either https://developer-specs.company-information.service.gov.uk/companies-house-public-data-api/reference/company-profile/company-profile or https://developer-specs.company-information.service.gov.uk/companies-house-public-data-api/reference/registered-office-address/registered-office-address
I need to write Java Spring Boot code and I need to be able to test is standalone during dev but then later I need to move the code/components etc. into a larger Java project
```