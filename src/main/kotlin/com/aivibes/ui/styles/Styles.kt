package com.aivibes.ui.styles

object Styles {
    const val CSS = """
        :root {
            --primary-color: #2563eb;
            --secondary-color: #1e40af;
            --text-color: #1f2937;
            --light-text: #6b7280;
            --background: #f9fafb;
            --card-bg: #ffffff;
            --border-color: #e5e7eb;
        }

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        html, body {
            height: 100%;
            width: 100%;
        }

        body {
            font-family: 'Inter', sans-serif;
            background-color: var(--background);
            color: var(--text-color);
            line-height: 1.6;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 2rem;
        }

        .header {
            background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
            color: white;
            padding: 4rem 2rem;
            text-align: center;
            position: relative;
            overflow: hidden;
        }

        .header::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="40" fill="rgba(255,255,255,0.1)"/></svg>') repeat;
            opacity: 0.1;
        }

        .header h1 {
            font-size: 3rem;
            font-weight: 700;
            margin-bottom: 1rem;
            position: relative;
        }

        .header p {
            font-size: 1.25rem;
            opacity: 0.9;
            max-width: 600px;
            margin: 0 auto;
            position: relative;
        }

        .article-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 2rem;
            margin-top: 2rem;
        }

        .article-card {
            background: var(--card-bg);
            border-radius: 1rem;
            overflow: hidden;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease;
        }

        .article-card:hover {
            transform: translateY(-5px);
        }

        .article-image {
            width: 100%;
            height: 200px;
            object-fit: cover;
            background: linear-gradient(45deg, #f3f4f6, #e5e7eb);
        }

        .article-content {
            padding: 1.5rem;
        }

        .article-title {
            font-size: 1.5rem;
            font-weight: 600;
            margin-bottom: 0.5rem;
            color: var(--text-color);
        }

        .article-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
            color: var(--light-text);
            font-size: 0.875rem;
        }

        .article-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 0.5rem;
            margin-top: 1rem;
        }

        .tag {
            background: #e0e7ff;
            color: var(--primary-color);
            padding: 0.25rem 0.75rem;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 500;
        }

        .featured-article {
            grid-column: 1 / -1;
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 2rem;
            align-items: center;
        }

        .featured-article .article-image {
            height: 100%;
            min-height: 300px;
        }

        .featured-article .article-content {
            padding: 2rem;
        }

        .featured-article .article-title {
            font-size: 2rem;
        }

        .nav-bar {
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(10px);
            padding: 1rem 2rem;
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            z-index: 1000;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .nav-logo {
            font-size: 1.5rem;
            font-weight: 700;
            color: white;
            text-decoration: none;
        }

        .nav-links {
            display: flex;
            gap: 2rem;
        }

        .nav-link {
            color: white;
            text-decoration: none;
            font-weight: 500;
            transition: opacity 0.3s ease;
        }

        .nav-link:hover {
            opacity: 0.8;
        }

        @media (max-width: 768px) {
            .container {
                padding: 1rem;
            }

            .header {
                padding: 3rem 1rem;
            }

            .header h1 {
                font-size: 2rem;
            }

            .article-grid {
                grid-template-columns: 1fr;
            }

            .featured-article {
                grid-template-columns: 1fr;
            }

            .featured-article .article-image {
                min-height: 200px;
            }
        }
    """
} 