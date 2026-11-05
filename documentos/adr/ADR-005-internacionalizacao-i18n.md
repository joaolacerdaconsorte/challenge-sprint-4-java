# ADR-005: Internacionalização Nativa (i18n PT-BR & EN) com SessionLocaleResolver

- **Status:** Aceito (Novo na Sprint 4)
- **Data:** Outubro de 2026
- **Decisores:** Equipe PetCare 360 — Turma 2TDSPW

## Contexto e Problema
Seguindo as boas práticas apresentadas em aula na disciplina **Java Advanced** (`lgsreal/2tdspw-mvc`), sistemas web modernos devem desacoplar textos de interface do código HTML, suportando múltiplos idiomas dinamicamente na sessão do usuário.

## Decisão
Implementamos a classe `InternacionalizacaoConfig` (`WebMvcConfigurer`) utilizando:
- **`SessionLocaleResolver`:** Define `pt_BR` como locale padrão da sessão.
- **`LocaleChangeInterceptor`:** Intercepta o parâmetro de query `?lang=pt_BR` ou `?lang=en` em qualquer rota e alterna o idioma instantaneamente.
- **`ResourceBundleMessageSource`:** Carrega os bundles UTF-8 `i18n/messages_pt_BR.properties` e `i18n/messages_en.properties`, consumidos nos templates Thymeleaf via expressão `#{...}`.

## Justificativa e Consequências
- **Experiência Global (UX):** Permite alternar entre Português (`PT-BR`) e Inglês (`EN`) com 1 clique no cabeçalho da aplicação e na tela de Login.
- **Manutenibilidade:** Textos institucionais e rótulos de navegação ficam centralizados em arquivos `.properties`.
