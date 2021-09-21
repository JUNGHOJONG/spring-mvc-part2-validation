package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v1/items")
@RequiredArgsConstructor
public class ValidationItemControllerV1 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v1/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v1/addForm";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {

        Map<String, String> errors = new HashMap<>();

        validateFieldAndGlobalLogic(item, errors);

        if (!errors.isEmpty()) {
            log.info("errors={}", errors);
            model.addAttribute("errors", errors);
            return "/validation/v1/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v1/items/{itemId}";
    }

    private void validateFieldAndGlobalLogic(Item item, Map<String, String> errors) {
        // 필드 검증
        if (!StringUtils.hasText(item.getItemName())) {
            errors.put("itemName", "상품명은 공백이 허용되지 않습니다.");
        }

        Integer price = item.getPrice();

        if (price != null && (price < 1000 || price > 1000000)) {
            errors.put("price", "가격은 1000원 이상 1백만원 이하만 허용합니다.");
        }

        Integer quantity = item.getQuantity();

        if (quantity != null && quantity > 9999) {
            errors.put("quantity", "수량은 최대 9999까지만 허용합니다.");
        }

        // 글로벌 검증
        if (price != null && quantity != null && price * quantity < 10000) {
            errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야 합니다.");
        }
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item, Model model) {

        Map<String, String> errors = new HashMap<>();

        validateFieldAndGlobalLogic(item, errors);

        if (!errors.isEmpty()) {
            log.info("errors={}", errors);
            model.addAttribute("errors", errors);
            return "/validation/v1/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v1/items/{itemId}";
    }

}

