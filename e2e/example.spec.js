const { test, expect } = require('@playwright/test');

test('homepage has expected title', async ({ page }) => {
  await page.goto('http://localhost:8080?test=true&reset_data=true');
  await expect(page).toHaveTitle(/Pheide/);
});
