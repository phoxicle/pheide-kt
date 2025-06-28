const { test, expect } = require('@playwright/test');

test.beforeAll(async ({ browser }) => {
  const page = await browser.newPage();
  // Go to login page
  await page.goto('http://localhost:8080?controller=auth&action=login');
  // Fill in credentials and submit
  await page.fill('input[name="username"]', 'admin');
  await page.fill('input[name="password"]', 'pass');
  await page.click('input[type="submit"]');
  // Go to reset URL
  await page.goto('http://localhost:8080?controller=admin&action=reset');
  await page.close();
});

test('homepage has expected title', async ({ page }) => {
  await page.goto('http://localhost:8080');
  await expect(page).toHaveTitle(/Pheide/);
});
